package org.gameswap.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.eclipse.jetty.servlet.FilterHolder;
import org.gameswap.model.User;
import org.gameswap.model.UserPrincipal;
import org.gameswap.persistance.UserDAO;
import org.gameswap.web.authentication.AuthFilter;
import org.gameswap.web.authentication.SimpleAuthenticator;
import org.gameswap.web.resource.AuthResource;
import org.gameswap.web.resource.TestResource;
import org.gameswap.web.resource.UserResource;

import java.io.IOException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.client.JerseyClientBuilder;
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
        bootstrap.addBundle(new AssetsBundle("/assets/app/", "/", "index.html"));
        bootstrap.addBundle(hibernateBundle);
        addMigrations(bootstrap);
        ObjectMapper mapper = bootstrap.getObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
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
        if (configuration.isRedirectAllToHttps()) {
            addHttpsForward(environment.getApplicationContext());
        }
        addAuthFilter(environment);
        environment.jersey().setUrlPattern("/gameswap/*");
        final Client client = new JerseyClientBuilder(environment).using(configuration.getJerseyClient()).build(getName());
        UserDAO dao = new UserDAO(hibernateBundle.getSessionFactory());

        registerResources(configuration, environment, client, dao);
        environment.jersey().register(new BasicCredentialAuthFilter.Builder<UserPrincipal>().setAuthenticator(new SimpleAuthenticator(dao)));
    }


    private void registerResources(GameswapConfiguration configuration, Environment environment, Client client, UserDAO dao) {
        environment.jersey().register(new UserResource(dao));
        environment.jersey().register(new TestResource());
        environment.jersey().register(new AuthResource(client, dao, configuration));
    }


    private void enableEnvironmentConfiguration(Bootstrap<GameswapConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
    }


    private void addAuthFilter(Environment environment) {
        environment.servlets().addFilter("AuthFilter", new AuthFilter()).addMappingForUrlPatterns(null, true, "/gameswap/v1/*");

    }


    private void addHttpsForward(MutableServletContextHandler handler) {
        handler.addFilter(new FilterHolder(new Filter() {

            public void init(FilterConfig filterConfig) throws ServletException {
            }


            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                StringBuffer uri = ((HttpServletRequest) request).getRequestURL();
                if (uri.toString().startsWith("http://")) {
                    String location = "https://" + uri.substring("http://".length());
                    ((HttpServletResponse) response).sendRedirect(location);
                } else {
                    chain.doFilter(request, response);
                }
            }


            public void destroy() {
            }
        }), "/*", EnumSet.allOf(DispatcherType.class));
    }
}
