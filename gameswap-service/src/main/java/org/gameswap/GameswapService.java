package org.gameswap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.gameswap.config.GameswapConfiguration;
import org.gameswap.daos.UserDAO;
import org.gameswap.models.User;
import org.gameswap.models.UserPrincipal;
import org.gameswap.resources.UserResource;
import org.gameswap.security.SimpleAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        environment.jersey().setUrlPattern("/gameswap/*");

        UserDAO dao = new UserDAO(hibernateBundle.getSessionFactory());
        environment.jersey().register(new UserResource(dao));

        environment.jersey().register(new BasicCredentialAuthFilter.Builder<UserPrincipal>().setAuthenticator(new SimpleAuthenticator(dao)));
    }
}
