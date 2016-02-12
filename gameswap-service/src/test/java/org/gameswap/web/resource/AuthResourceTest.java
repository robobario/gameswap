package org.gameswap.web.resource;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.CharStreams;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.eclipse.jetty.http.HttpStatus;
import org.gameswap.application.GameswapConfiguration;
import org.gameswap.model.Role;
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
import java.text.ParseException;

import static org.assertj.core.api.Assertions.assertThat;
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
        User user = createUser(KNOWN_USER, PASSWORD_HASHED);
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

    @Test
    public void testSignUpUnsuccessful_UserExists() throws IOException {
        User user = createUser(KNOWN_USER, "password");
        Response response = requestSignup(user);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
        assertThat(getStringResponse(response)).contains(AuthResource.USERNAME_EXISTS);
        verify(dao).findByName(KNOWN_USER);
    }

    @Test
    public void testSignUpUnsuccessful_PasswordTooShort() throws IOException {
        User user = createUser("unknownUser", "pass1");
        Response response = requestSignup(user);
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(getStringResponse(response)).contains("password size must be between 6 and 100");
    }

    @Test
    public void testSignupUnsuccessful_UsernameTooShort() throws IOException {
        User user = createUser("user1", PASSWORD);
        Response response = requestSignup(user);
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(getStringResponse(response)).contains("username size must be between 6 and 60");
    }

    @Test
    public void testSignUpSuccessful() throws IOException, ParseException, JOSEException {
        User newUser = createUser("newUser", PASSWORD_HASHED);
        when(dao.save(any(User.class))).thenReturn(newUser);

        User userToSignUp = createUser("newUser", PASSWORD);
        Response response = requestSignup(userToSignUp);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED_201);
        verify(dao).findByName("newUser");
    }

    @Test
    public void testLoginToken() throws Exception {
        User user = createUser(KNOWN_USER, PASSWORD);
        Response response = requestLogin(user);
        JWTClaimsSet claimsSet = getJwtClaimsSetFromResponse(response);
        assertThat(claimsSet.getClaims()).containsKeys("sub", "name", "role");
        assertThat(claimsSet.getClaim("sub")).isEqualTo("0"); //Id generated during test
        assertThat(claimsSet.getClaim("name")).isEqualTo(KNOWN_USER);
        assertThat(claimsSet.getClaim("role")).isEqualTo(Role.USER);
    }

    @Test
    public void testSignUpToken() throws Exception {
        User newUser = createUser("newUser", PASSWORD_HASHED);
        when(dao.save(any(User.class))).thenReturn(newUser);

        User userToSignUp = createUser("newUser", PASSWORD);
        Response response = requestSignup(userToSignUp);
        JWTClaimsSet claimsSet = getJwtClaimsSetFromResponse(response);
        assertThat(claimsSet.getClaims()).containsKeys("sub", "name", "role");
        assertThat(claimsSet.getClaim("sub")).isEqualTo("0"); //id generated during test
        assertThat(claimsSet.getClaim("name")).isEqualTo("newUser");
        assertThat(claimsSet.getClaim("role")).isEqualTo(Role.USER);
    }


    private JWTClaimsSet getJwtClaimsSetFromResponse(Response response) throws IOException, ParseException, JOSEException {
        JSONObject jsonObject = getJsonResponse(response);
        String token = jsonObject.getString("token");
        return AuthUtils.decodeToken("Authorization: " + token);
    }

    private Response requestLogin(User user) {
        Entity entity = Entity.entity(user, MediaType.APPLICATION_JSON_TYPE);
        return resources.client().target("/auth/login").request(MediaType.APPLICATION_JSON_TYPE)
                        .post(entity);
    }

    private Response requestSignup(User user) {
        Entity entity = Entity.entity(user, MediaType.APPLICATION_JSON_TYPE);
        return resources.client().target("/auth/signup").request(MediaType.APPLICATION_JSON_TYPE)
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
        user.setDisplayName(username);
        user.setPassword(password);
        user.setRole(Role.USER);
        return user;
    }
}