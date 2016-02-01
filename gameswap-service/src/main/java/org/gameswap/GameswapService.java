package org.gameswap;

import org.gameswap.config.*;
import org.gameswap.daos.*;
import org.gameswap.models.*;
import org.gameswap.resources.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Optional;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameswapService extends Application<GameswapConfiguration> {
    private static final Logger LOG = LoggerFactory.getLogger(GameswapService.class);

    public static void main(String[] args) throws Exception {
        new GameswapService().run(args);
    }

    private final HibernateBundle<GameswapConfiguration> hibernateBundle = new HibernateBundle<GameswapConfiguration>(
            
            User.class,
            Void.class
        ) {
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
    public void run(GameswapConfiguration configuration,
                    Environment environment) throws Exception {
        environment.jersey().setUrlPattern("/gameswap/*");

        environment.jersey().register(new UserResource(
            new UserDAO(hibernateBundle.getSessionFactory())));
    }
}
