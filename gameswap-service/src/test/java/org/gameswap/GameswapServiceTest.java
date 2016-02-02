package org.gameswap;


import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.gameswap.config.GameswapConfiguration;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class GameswapServiceTest {

    @Rule
    public final DropwizardAppRule<GameswapConfiguration> RULE =
            new DropwizardAppRule<>(GameswapService.class,
                    ResourceHelpers.resourceFilePath("https.yaml"));

    @Test
    public void allTrafficIsRedirectedToHttpsIfConfigured() {
        Client client = new JerseyClientBuilder().build();
        Response response = client.target(
                String.format("http://localhost:%d/login/auth", RULE.getLocalPort())
        ).request().get();
        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getHeaderString("Location")).startsWith("https");
    }
}