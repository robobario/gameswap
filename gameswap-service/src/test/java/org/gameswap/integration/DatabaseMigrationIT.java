package org.gameswap.integration;

import org.gameswap.application.GameswapConfiguration;
import org.gameswap.application.GameswapService;
import org.junit.Rule;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

public class DatabaseMigrationIT {

    @Rule
    public final DropwizardAppRule<GameswapConfiguration> RULE =
            new DropwizardAppRule<>(GameswapService.class,
                    ResourceHelpers.resourceFilePath("migration-test.yaml"));

    @Test
    public void test() throws LiquibaseException, SQLException {
        Connection name = RULE.getConfiguration().getDataSourceFactory().build(null, "name").getConnection();
        Liquibase liquibase = new Liquibase("migrations.xml", new ClassLoaderResourceAccessor(), new JdbcConnection(name));
        liquibase.updateTestingRollback("");
    }
}
