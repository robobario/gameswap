package org.gameswap.web.resource;

import com.google.common.base.Optional;
import com.google.gson.JsonObject;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.eclipse.jetty.http.HttpStatus;
import org.gameswap.application.GameswapConfiguration;
import org.gameswap.model.Role;
import org.gameswap.model.User;
import org.gameswap.persistance.UserDAO;
import org.gameswap.security.PasswordService;
import org.gameswap.web.authentication.JwtTokenCoder;
import org.glassfish.jersey.client.JerseyClient;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.ParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class AuthResourceTest {

    private static final HttpServletRequest request = mock(HttpServletRequest.class);
    private static final String PASSWORD = "password";
    private static final String PASSWORD_HASHED = PasswordService.hashPassword(PASSWORD);
    private static final String KNOWN_USER = "somebody";
    private static final JwtTokenCoder JWT_TOKEN_CODER = new JwtTokenCoder("aliceinwonderlandhajiddiwhatnowzaheyheyhey");
    private static GameswapConfiguration config = mock(GameswapConfiguration.class);
    private static UserDAO dao = mock(UserDAO.class);
    private static Client client = mock(JerseyClient.class);
    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
                                                                     .addResource(new AuthResource(client, dao, config, JWT_TOKEN_CODER))
                                                                     .addProvider(new ContextInjectableProvider<>(HttpServletRequest.class, request))
                                                                     .build();


    @Before
    public void setup() {
        reset(dao);
        User user = Api.createUser(KNOWN_USER, PASSWORD_HASHED);
        when(dao.findByName(anyString())).thenReturn(Optional.absent());
        when(dao.findByName(eq(KNOWN_USER))).thenReturn(Optional.of(user));
    }


    @Test
    public void testLoginSuccessful() throws IOException {
        User user = Api.createUser(KNOWN_USER, PASSWORD);
        Response response = requestLogin(user);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        assertThat(Api.helpers().getStringResponse(response)).contains("token");
        verify(dao).findByName(KNOWN_USER);
    }

    @Test
    public void testLoginUnSuccessful_BadPassword() throws IOException {
        User user = Api.createUser(KNOWN_USER, "badpassword");
        Response response = requestLogin(user);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
        assertThat(Api.helpers().getStringResponse(response)).contains(AuthResource.LOGGING_ERROR_MSG);
        verify(dao).findByName(KNOWN_USER);
    }

    @Test
    public void testLoginUnSuccessful_BadLogin() throws IOException {
        User user = Api.createUser("otherUser", PASSWORD);
        Response response = requestLogin(user);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
        assertThat(Api.helpers().getStringResponse(response)).contains(AuthResource.LOGGING_ERROR_MSG);
        verify(dao).findByName("otherUser");
    }

    @Test
    public void testSignUpUnsuccessful_UserExists() throws IOException {
        User user = Api.createUser(KNOWN_USER, "password");
        Response response = requestSignup(user);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
        assertThat(Api.helpers().getStringResponse(response)).contains(AuthResource.USERNAME_EXISTS);
        verify(dao).findByName(KNOWN_USER);
    }

    @Test
    public void testSignUpUnsuccessful_PasswordTooShort() throws IOException {
        User user = Api.createUser("unknownUser", "pass1");
        Response response = requestSignup(user);
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(Api.helpers().getStringResponse(response)).contains("password size must be between 6 and 100");
    }

    @Test
    public void testSignupUnsuccessful_UsernameTooShort() throws IOException {
        User user = Api.createUser("user1", PASSWORD);
        Response response = requestSignup(user);
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(Api.helpers().getStringResponse(response)).contains("username size must be between 6 and 60");
    }

    @Test
    public void testSignupUnsuccessful_EmptyUsername() throws IOException {
        User user = Api.createUser("", PASSWORD);
        Response response = requestSignup(user);
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(Api.helpers().getStringResponse(response)).contains("username size must be between 6 and 60");
    }

    @Test
    public void testSignupUnsuccessful_EmptyPassword() throws IOException {
        User user = Api.createUser("newUser", "");
        Response response = requestSignup(user);
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(Api.helpers().getStringResponse(response)).contains("password size must be between 6 and 100");
    }

    @Test
    public void testSignUpSuccessful() throws IOException, ParseException, JOSEException {
        User newUser = Api.createUser("newUser", PASSWORD_HASHED);
        when(dao.save(any(User.class))).thenReturn(newUser);

        User userToSignUp = Api.createUser("newUser", PASSWORD);
        Response response = requestSignup(userToSignUp);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED_201);
        verify(dao).findByName("newUser");
    }

    @Test
    public void testLoginToken() throws Exception {
        User user = Api.createUser(KNOWN_USER, PASSWORD);
        Response response = requestLogin(user);
        JWTClaimsSet claimsSet = getJwtClaimsSetFromResponse(response);
        assertThat(claimsSet.getClaims()).containsKeys("sub", "name", "role");
        assertThat(claimsSet.getClaim("sub")).isEqualTo("0"); //Id generated during test
        assertThat(claimsSet.getClaim("name")).isEqualTo(KNOWN_USER);
        assertThat(claimsSet.getClaim("role")).isEqualTo(Role.USER);
    }

    @Test
    public void testSignUpToken() throws Exception {
        User newUser = Api.createUser("newUser", PASSWORD_HASHED);
        when(dao.save(any(User.class))).thenReturn(newUser);

        User userToSignUp = Api.createUser("newUser", PASSWORD);
        Response response = requestSignup(userToSignUp);
        JWTClaimsSet claimsSet = getJwtClaimsSetFromResponse(response);
        assertThat(claimsSet.getClaims()).containsKeys("sub", "name", "role");
        assertThat(claimsSet.getClaim("sub")).isEqualTo("0"); //id generated during test
        assertThat(claimsSet.getClaim("name")).isEqualTo("newUser");
        assertThat(claimsSet.getClaim("role")).isEqualTo(Role.USER);
    }


    private JWTClaimsSet getJwtClaimsSetFromResponse(Response response) throws IOException, ParseException, JOSEException {
        JsonObject jsonObject = (JsonObject) Api.helpers().getJsonResponse(response);
        String token = jsonObject.get("token").getAsString();
        return JWT_TOKEN_CODER.decodeToken("Authorization: " + token);
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


}