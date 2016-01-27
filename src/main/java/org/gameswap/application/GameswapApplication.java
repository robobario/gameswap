package org.gameswap.application;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.gameswap.resource.TestResource;

public class GameswapApplication extends Application<GameswapConfiguration> {

    public static void main(String[] args) throws Exception {
        new GameswapApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<GameswapConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/static/", "/", "index.html"));
    }

    @Override
    public void run(GameswapConfiguration watcherConfiguration, Environment environment) throws Exception {
        environment.jersey().register(new TestResource(environment.getObjectMapper()));
    }


    @Override
    public String getName() {
        return "kafka watcher";
    }
}
