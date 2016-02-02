package org.gameswap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlet.FilterHolder;
import org.gameswap.config.GameswapConfiguration;
import org.gameswap.daos.UserDAO;
import org.gameswap.models.User;
import org.gameswap.models.UserPrincipal;
import org.gameswap.resources.UserResource;
import org.gameswap.security.SimpleAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.EnumSet;

public class GameswapService extends Application<GameswapConfiguration> {

    private static final Logger LOG = LoggerFactory.getLogger(GameswapService.class);


    public static void main(String[] args) throws Exception {
        new GameswapService().run(args);
    }

    private final HibernateBundle<GameswapConfiguration> hibernateBundle = new HibernateBundle<GameswapConfiguration>(

    User.class, Void.class) {

        @Override
        public DataSourceFactory getDataSourceFactory(GameswapConfiguration configuration) {
            return configuration.getDataSourceFactory();
        }
    };


    @Override
    public String getName() {
        return "gameswap";
    }


    @Override
    public void initialize(Bootstrap<GameswapConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets/app/", "/", "index.html"));
        bootstrap.addBundle(hibernateBundle);
        ObjectMapper mapper = bootstrap.getObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }


    @Override
    public void run(GameswapConfiguration configuration, Environment environment) throws Exception {
        if (configuration.isRedirectAllToHttps()) {
            addHttpsForward(environment.getApplicationContext());
        }
        environment.jersey().setUrlPattern("/gameswap/*");

        UserDAO dao = new UserDAO(hibernateBundle.getSessionFactory());
        environment.jersey().register(new UserResource(dao));

        environment.jersey().register(new BasicCredentialAuthFilter.Builder<UserPrincipal>().setAuthenticator(new SimpleAuthenticator(dao)));
    }

    private void addHttpsForward(MutableServletContextHandler handler) {
        handler.addFilter(new FilterHolder(new Filter() {

            public void init(FilterConfig filterConfig) throws ServletException {
            }

            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
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
