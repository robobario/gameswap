package org.gameswap.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlet.FilterHolder;
import org.gameswap.messaging.MessageConsumer;
import org.gameswap.messaging.MessageSender;
import org.gameswap.messaging.Messagers;
import org.gameswap.messaging.Result;
import org.gameswap.messaging.model.VerifyResponse;
import org.gameswap.web.authentication.OneTimePasswordMaker;
import org.gameswap.web.model.User;
import org.gameswap.web.model.UserPrincipal;
import org.gameswap.persistance.UserDAO;
import org.gameswap.web.HttpsForwardingFilter;
import org.gameswap.web.authentication.JwtAuthFilter;
import org.gameswap.web.authentication.JwtTokenCoder;
import org.gameswap.web.resource.AuthResource;
import org.gameswap.web.resource.TestResource;
import org.gameswap.web.resource.UserResource;
import org.gameswap.web.resource.VerifyResource;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.servlet.DispatcherType;
import javax.ws.rs.client.Client;
import java.util.EnumSet;
import java.util.Objects;

public class GameswapService extends Application<GameswapConfiguration> {

    private final HibernateBundle<GameswapConfiguration> hibernateBundle = new HibernateBundle<GameswapConfiguration>(User.class, Void.class) {

        @Override
        public DataSourceFactory getDataSourceFactory(GameswapConfiguration configuration) {
            return configuration.getDataSourceFactory();
        }
    };

    public static void main(String[] args) throws Exception {
        new GameswapService().run(args);
    }

    @Override
    public String getName() {
        return "gameswap";
    }


    @Override
    public void initialize(Bootstrap<GameswapConfiguration> bootstrap) {
        enableEnvironmentConfiguration(bootstrap);
        addBundles(bootstrap);
        customiseObjectMapper(bootstrap);
    }

    private void customiseObjectMapper(Bootstrap<GameswapConfiguration> bootstrap) {
        ObjectMapper mapper = bootstrap.getObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private void addBundles(Bootstrap<GameswapConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets/app/", "/", "index.html"));
        bootstrap.addBundle(hibernateBundle);
        addMigrations(bootstrap);
    }

    private void addMigrations(Bootstrap<GameswapConfiguration> bootstrap) {
        bootstrap.addBundle(new MigrationsBundle<GameswapConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(GameswapConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
    }


    @Override
    public void run(GameswapConfiguration configuration, Environment environment) throws Exception {
        addFilters(configuration, environment);
        environment.jersey().setUrlPattern("/gameswap/*");
        final Client client = new JerseyClientBuilder(environment).using(configuration.getJerseyClient())
                                                                  .build(getName());
        client.property(ClientProperties.READ_TIMEOUT, configuration.getSocketReadTimeoutMillis());
        SessionFactory sessionFactory = getSessionFactory();
        UserDAO dao = new UserDAO(sessionFactory);
        JwtTokenCoder jwtTokenCoder = new JwtTokenCoder(configuration.getJwtSecret());
        registerResources(configuration, environment, client, dao, jwtTokenCoder);

        if(configuration.isRabbitMqEnabled()) {
            MessageSender sender = Messagers.sender(configuration.getRabbitMqSendUrl(), environment.getObjectMapper());
            sender.init();
            initialiseMessageConsumer(environment, sessionFactory, configuration);
            addVerificationResource(environment, dao, sender);
        }

        environment.jersey()
                   .register(new AuthDynamicFeature(new JwtAuthFilter(dao, getSessionFactory(), jwtTokenCoder)));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(UserPrincipal.class));
    }

    private void initialiseMessageConsumer(Environment environment, SessionFactory sessionFactory, GameswapConfiguration configuration) {
        MessageConsumer consumer = Messagers.consumer(configuration.getRabbitMqReceiveUrl(), environment.getObjectMapper());
        consumer.init();
        consumer.register(i -> onVerifyResponse(i, sessionFactory), VerifyResponse.class, "verifyResponse");
    }

    private Result onVerifyResponse(VerifyResponse response, SessionFactory sessionFactory) {
        Session session = sessionFactory.openSession();
        Transaction transaction  = null;
        try {
            transaction = session.beginTransaction();
            User user = (User) session.get(User.class, response.getAccountId());
            if(user != null){
                if(Objects.equals(user.getBggUserName(), response.getBggUserName())){
                    if(response.isSuccess()){
                        user.setBggVerified(true);
                        session.save(user);
                    }
                }
            }
            transaction.commit();
            return new Result(true, null);
        }catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            return new Result(false, e.getMessage());
        }finally
         {
            session.close();
        }
    }

    private void addVerificationResource(Environment environment, UserDAO dao, MessageSender sender) {
        environment.jersey().register(new VerifyResource(sender,dao, new OneTimePasswordMaker()));
    }

    public SessionFactory getSessionFactory() {
        return hibernateBundle.getSessionFactory();
    }

    private void addFilters(GameswapConfiguration configuration, Environment environment) {
        if (configuration.isRedirectAllToHttps()) {
            addHttpsForward(environment.getApplicationContext());
        }
    }


    private void registerResources(GameswapConfiguration configuration, Environment environment, Client client, UserDAO dao, JwtTokenCoder jwtTokenCoder) {
        environment.jersey().register(new UserResource(dao));
        environment.jersey().register(new TestResource());
        environment.jersey().register(new AuthResource(client, dao, configuration, jwtTokenCoder));
    }


    private void enableEnvironmentConfiguration(Bootstrap<GameswapConfiguration> bootstrap) {
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor(false);
        ConfigurationSourceProvider provider = bootstrap.getConfigurationSourceProvider();
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(provider, substitutor));
    }


    private void addHttpsForward(MutableServletContextHandler handler) {
        handler.addFilter(new FilterHolder(new HttpsForwardingFilter()), "/*", EnumSet.allOf(DispatcherType.class));
    }
}
