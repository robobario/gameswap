package org.gameswap.application;


import com.nimbusds.jose.JOSEException;

import org.gameswap.model.Token;
import org.gameswap.model.User;
import org.gameswap.web.authentication.AuthUtils;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.Rule;
import org.junit.Test;

import java.text.ParseException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

public class GameswapServiceTest {

    @Rule
    public final DropwizardAppRule<GameswapConfiguration> RULE =
            new DropwizardAppRule<>(GameswapService.class,
                    ResourceHelpers.resourceFilePath("https.yaml"));
    @Rule
    public final DropwizardAppRule<GameswapConfiguration> HTTP =
            new DropwizardAppRule<>(GameswapService.class,
                    ResourceHelpers.resourceFilePath("http.yaml"));

    @Test
    public void allTrafficIsRedirectedToHttpsIfConfigured() {
        Client client = new JerseyClientBuilder().build();
        Response response = client.target(
                format("http://localhost:%d/login/auth", RULE.getLocalPort())
        ).request().get();
        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getHeaderString("Location")).startsWith("https");
    }

    @Test
    public void signup() throws ParseException, JOSEException {
        Client client = new JerseyClientBuilder().build();
        User entity = new User();
        entity.setUsername("username");
        entity.setPassword("password");
        Response response = client.target(
                format("http://localhost:%d/gameswap/auth/signup", HTTP.getLocalPort())
        ).request().post(Entity.json(entity));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        String token = response.readEntity(Token.class).getToken();
        assertThat(token).isNotNull();
        assertDecodable(token);
    }

    @Test
    public void login() throws ParseException, JOSEException {
        Client client = new JerseyClientBuilder().build();
        User entity = new User();
        entity.setUsername("username");
        entity.setPassword("password");
        client.target(
                format("http://localhost:%d/gameswap/auth/signup", HTTP.getLocalPort())
        ).request().post(Entity.json(entity));
        client.target(format("http://localhost:%d/gameswap/auth/logout", HTTP.getLocalPort())).request().get();
        Response login = client.target(
                format("http://localhost:%d/gameswap/auth/login", HTTP.getLocalPort())
        ).request().post(Entity.json(entity));
        assertThat(login.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String token = login.readEntity(Token.class).getToken();
        assertThat(token).isNotNull();
        assertDecodable(token);
    }

    private void assertDecodable(String token) throws ParseException, JOSEException {
        AuthUtils.decodeToken("Authorization: " + token);
    }
}