package org.gameswap.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.eclipse.jetty.servlet.FilterHolder;
import org.gameswap.model.User;
import org.gameswap.model.UserPrincipal;
import org.gameswap.persistance.UserDAO;
import org.gameswap.web.HttpsForwardingFilter;
import org.gameswap.web.authentication.AuthFilter;
import org.gameswap.web.authentication.SimpleAuthenticator;
import org.gameswap.web.resource.AuthResource;
import org.gameswap.web.resource.TestResource;
import org.gameswap.web.resource.UserResource;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.ws.rs.client.Client;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
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
        final Client client = new JerseyClientBuilder(environment).using(configuration.getJerseyClient()).build(getName());
        UserDAO dao = new UserDAO(hibernateBundle.getSessionFactory());
        registerResources(configuration, environment, client, dao);
        environment.jersey().register(new BasicCredentialAuthFilter.Builder<UserPrincipal>().setAuthenticator(new SimpleAuthenticator(dao)));
    }

    private void addFilters(GameswapConfiguration configuration, Environment environment) {
        if (configuration.isRedirectAllToHttps()) {
            addHttpsForward(environment.getApplicationContext());
        }
        addAuthFilter(environment);
    }


    private void registerResources(GameswapConfiguration configuration, Environment environment, Client client, UserDAO dao) {
        environment.jersey().register(new UserResource(dao));
        environment.jersey().register(new TestResource());
        environment.jersey().register(new AuthResource(client, dao, configuration));
    }


    private void enableEnvironmentConfiguration(Bootstrap<GameswapConfiguration> bootstrap) {
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor(false);
        ConfigurationSourceProvider provider = bootstrap.getConfigurationSourceProvider();
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(provider, substitutor));
    }


    private void addAuthFilter(Environment environment) {
        environment.servlets().addFilter("AuthFilter", new AuthFilter()).addMappingForUrlPatterns(null, true, "/gameswap/v1/*");

    }


    private void addHttpsForward(MutableServletContextHandler handler) {
        handler.addFilter(new FilterHolder(new HttpsForwardingFilter()), "/*", EnumSet.allOf(DispatcherType.class));
    }

}
