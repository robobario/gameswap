package org.gameswap.web.resource;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.CharStreams;
import com.nimbusds.jwt.JWTClaimsSet;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.eclipse.jetty.http.HttpStatus;
import org.gameswap.application.GameswapConfiguration;
import org.gameswap.model.User;
import org.gameswap.persistance.UserDAO;
import org.gameswap.security.PasswordService;
import org.gameswap.web.authentication.AuthUtils;
import org.glassfish.jersey.client.JerseyClient;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class AuthResourceTest {

    private static GameswapConfiguration config = mock(GameswapConfiguration.class);

    private static UserDAO dao = mock(UserDAO.class);

    private static Client client = mock(JerseyClient.class);

    private static final HttpServletRequest request = mock(HttpServletRequest.class);

    private static final String PASSWORD = "password";

    private static final String PASSWORD_HASHED = PasswordService.hashPassword(PASSWORD);

    private static final String KNOWN_USER = "somebody";

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
                                                                     .addResource(new AuthResource(client, dao, config))
                                                                     .addProvider(new ContextInjectableProvider<>(HttpServletRequest.class, request))

                                                                     .build();


    @Before
    public void setup() {
        reset(dao);
        User user = createUser("testUser", PASSWORD_HASHED);
        when(dao.findByName(anyString())).thenReturn(Optional.absent());
        when(dao.findByName(eq(KNOWN_USER))).thenReturn(Optional.of(user));
    }


    @Test
    public void testLoginSuccessful() throws IOException {
        User user = createUser(KNOWN_USER, PASSWORD);
        Response response = requestLogin(user);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        assertThat(getStringResponse(response)).contains("token");
        verify(dao).findByName(KNOWN_USER);
    }

    @Test
    public void testToken() throws Exception {
        User user = createUser(KNOWN_USER, PASSWORD);
        Response response = requestLogin(user);
        JSONObject jsonObject = getJsonResponse(response);
        String token = jsonObject.getString("token");
        JWTClaimsSet claimsSet = AuthUtils.decodeToken("Authorization: " + token);
        assertTrue(claimsSet.getClaims().containsKey("sub"));
    }

    @Test
    public void testLoginUnSuccessful_BadPassword() throws IOException {
        User user = createUser(KNOWN_USER, "badpassword");
        Response response = requestLogin(user);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
        assertThat(getStringResponse(response)).contains(AuthResource.LOGING_ERROR_MSG);
        verify(dao).findByName(KNOWN_USER);
    }

    @Test
    public void testLoginUnSuccessful_BadLogin() throws IOException {
        User user = createUser("otherUser", PASSWORD);
        Response response = requestLogin(user);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
        assertThat(getStringResponse(response)).contains(AuthResource.LOGING_ERROR_MSG);
        verify(dao).findByName("otherUser");
    }

    private Response requestLogin(User user) {
        Entity entity = Entity.entity(user, MediaType.APPLICATION_JSON_TYPE);
        return resources.client().target("/auth/login").request(MediaType.APPLICATION_JSON_TYPE)
                        .post(entity);
    }

    private JSONObject getJsonResponse(Response response) throws IOException {
        String stringResponse = getStringResponse(response);
        return new JSONObject(stringResponse);
    }

    private String getStringResponse(Response response) throws IOException {
        InputStreamReader stream = new InputStreamReader((ByteArrayInputStream) response.getEntity(), Charsets.UTF_8);
        return CharStreams.toString(stream);
    }


    private User createUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }
}