package org.gameswap.web.resource;

import com.google.common.base.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;

import org.apache.commons.lang3.StringUtils;
import org.gameswap.application.GameswapConfiguration;
import org.gameswap.web.model.Role;
import org.gameswap.web.model.Token;
import org.gameswap.web.model.User;
import org.gameswap.web.model.User.Provider;
import org.gameswap.persistance.UserDAO;
import org.gameswap.security.PasswordService;
import org.gameswap.web.authentication.JwtTokenCoder;
import org.hibernate.validator.constraints.NotBlank;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.errors.ErrorMessage;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    public static final String LOGGING_ERROR_MSG = "Wrong email and/or password";
    public static final String USERNAME_EXISTS = "account with name already exists";
    private static final String CLIENT_ID_KEY = "client_id";
    private static final String REDIRECT_URI_KEY = "redirect_uri";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String CODE_KEY = "code";
    private static final String GRANT_TYPE_KEY = "grant_type";
    private static final String AUTH_CODE = "authorization_code";
    private static final String CONFLICT_MSG = "There is already a %s account that belongs to you";
    private static final String NOT_FOUND_MSG = "User not found";
    private static final String UNLINK_ERROR_MSG = "Could not unlink %s account because it is your only sign-in method";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String GOOGLE_TOKEN_URL = "https://accounts.google.com/o/oauth2/token";
    private static final String GOOGLE_PEOPLE_API_URL = "https://www.googleapis.com/plus/v1/people/me/openIdConnect";
    private final Client client;
    private final UserDAO dao;
    private final GameswapConfiguration secrets;
    private JwtTokenCoder jwtTokenCoder;


    public AuthResource(final Client client, final UserDAO dao, final GameswapConfiguration secrets, JwtTokenCoder jwtTokenCoder) {
        this.client = client;
        this.dao = dao;
        this.secrets = secrets;
        this.jwtTokenCoder = jwtTokenCoder;
    }


    @POST
    @Path("login")
    @UnitOfWork
    public Response login(@Valid final User user, @Context final HttpServletRequest request) throws JOSEException {
        final Optional<User> foundUser = dao.findByName(user.getUsername());
        if (foundUser.isPresent() && PasswordService.checkPassword(user.getPassword(), foundUser.get().getPassword())) {
            return ok(createToken(request, foundUser.get()));
        }
        return unauthorized(LOGGING_ERROR_MSG);
    }


    @POST
    @Path("signup")
    @UnitOfWork
    public Response signup(@Valid final User user, @Context final HttpServletRequest request) throws JOSEException {
        Optional<User> existing = dao.findByName(user.getUsername());
        if (existing.isPresent()) {
            return unauthorized(USERNAME_EXISTS);
        }
        user.setRole(Role.USER);
        user.setPassword(PasswordService.hashPassword(user.getPassword()));
        user.setDisplayName(user.getUsername());
        final User savedUser = dao.save(user);
        final Token token = createToken(request, savedUser);
        return Response.status(Status.CREATED).entity(token).build();
    }

    @POST
    @Path("google")
    @UnitOfWork
    public Response loginGoogle(@Valid final Payload payload, @Context final HttpServletRequest request) throws JOSEException, ParseException,
            IOException {
        Response response = getAccessToken(payload, GOOGLE_TOKEN_URL);
        final Map<String, Object> userInfo = getUserInfo(GOOGLE_PEOPLE_API_URL, response);
        return processUser(request, Provider.GOOGLE, userInfo.get("sub").toString(), userInfo.get("name").toString());
    }


    @POST
    @Path("unlink/")
    @UnitOfWork
    public Response unlink(@Valid final UnlinkRequest unlinkRequest,
                           @Context final HttpServletRequest request) throws ParseException, IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException, SecurityException, JOSEException {
        final String subject = getSubject(request.getHeader(JwtTokenCoder.AUTH_HEADER_KEY));
        final Optional<User> foundUser = findUser(subject);
        String provider = unlinkRequest.provider;
        if (!foundUser.isPresent()) {
            return notFound();
        } else {
            return tryUnlinkExistingUser(foundUser.get(), provider);
        }
    }

    private Response tryUnlinkExistingUser(User userToUnlink, String provider) throws IllegalAccessException, NoSuchFieldException {
        if (isUserTryingToUnlinkOnlySigninMethod(userToUnlink)) {
            return Response.status(Status.BAD_REQUEST).entity(new ErrorMessage(String.format(UNLINK_ERROR_MSG, provider))).build();
        } else {
            return unlinkUser(userToUnlink, provider);
        }
    }

    private Response unlinkUser(User userToUnlink, String provider) {
        try {
            userToUnlink.setProviderId(Provider.valueOf(provider.toUpperCase()), null);
            dao.save(userToUnlink);
            return Response.ok().build();
        } catch (final IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST).build();
        }
    }

    private boolean isUserTryingToUnlinkOnlySigninMethod(User userToUnlink) throws IllegalAccessException, NoSuchFieldException {
        return userToUnlink.getSignInMethodCount() == 1;
    }


    private Map<String, Object> getUserInfo(String peopleApiUrl, Response response) throws IOException {
        final String accessToken = (String) getResponseEntity(response).get("access_token");
        response = client.target(peopleApiUrl).request("text/plain").header(JwtTokenCoder.AUTH_HEADER_KEY, String.format("Bearer %s", accessToken)).get();
        return getResponseEntity(response);
    }

    private Response getAccessToken(@Valid Payload payload, String accessTokenUrl) {
        final MultivaluedMap<String, String> accessData = new MultivaluedHashMap<>();
        accessData.add(CLIENT_ID_KEY, payload.getClientId());
        accessData.add(REDIRECT_URI_KEY, payload.getRedirectUri());
        accessData.add(CLIENT_SECRET, secrets.getGoogleSecret());
        accessData.add(CODE_KEY, payload.getCode());
        accessData.add(GRANT_TYPE_KEY, AUTH_CODE);
        Response response = client.target(accessTokenUrl).request().post(Entity.form(accessData));
        accessData.clear();
        return response;
    }

    private Map<String, Object> getResponseEntity(final Response response) throws IOException {
        return MAPPER.readValue(response.readEntity(String.class), new TypeReference<Map<String, Object>>() {
        });
    }

    private Response processUser(final HttpServletRequest request, final Provider provider, final String id,
                                 final String displayName) throws ParseException, JOSEException {
        final Optional<User> user = dao.findByProvider(provider, id);
        final String authHeader = request.getHeader(JwtTokenCoder.AUTH_HEADER_KEY);
        boolean isUserLoggedIn = StringUtils.isNotBlank(authHeader);
        if (isUserLoggedIn) {
            return linkProviderToLoggedInAccount(request, provider, id, displayName, user, authHeader);
        } else {
            return getOrCreateUser(request, provider, id, displayName, user);
        }

    }

    private Response getOrCreateUser(HttpServletRequest request, Provider provider, String id, String displayName, Optional<User> user) throws JOSEException {
        User userToSave;
        if (user.isPresent()) {
            userToSave = user.get();
        } else {
            userToSave = new User();
            userToSave.setRole(Role.USER);
            userToSave = setProviderAndSave(provider, id, displayName, userToSave);
        }
        final Token token = createToken(request, userToSave);
        return ok(token);
    }

    private Response linkProviderToLoggedInAccount(HttpServletRequest request, Provider provider, String id, String displayName, Optional<User> user, String authHeader) throws ParseException, JOSEException {
        if (user.isPresent()) {
            return Response.status(Status.CONFLICT).entity(new ErrorMessage(String.format(CONFLICT_MSG, provider.capitalize()))).build();
        }

        final String subject = getSubject(authHeader);
        final Optional<User> foundUser = findUser(subject);
        if (!foundUser.isPresent()) {
            return notFound();
        }

        User userToSave = foundUser.get();
        userToSave = setProviderAndSave(provider, id, displayName, userToSave);
        final Token token = createToken(request, userToSave);
        return ok(token);
    }

    private String getSubject(String authHeader) throws ParseException, JOSEException {
        return jwtTokenCoder.getSubject(authHeader);
    }

    private User setProviderAndSave(Provider provider, String id, String displayName, User userToSave) {
        userToSave.setProviderId(provider, id);
        if (userToSave.getDisplayName() == null) {
            userToSave.setDisplayName(displayName);
        }
        userToSave = dao.save(userToSave);
        return userToSave;
    }

    private Response notFound() {
        return Response.status(Status.NOT_FOUND).entity(new ErrorMessage(NOT_FOUND_MSG)).build();
    }

    private Optional<User> findUser(String subject) {
        return dao.find(Long.parseLong(subject));
    }


    private Response unauthorized(String message) {
        return Response.status(Status.UNAUTHORIZED).entity(new ErrorMessage(message)).build();
    }


    private Response ok(Object token) {
        return Response.ok().entity(token).build();
    }

    private Token createToken(HttpServletRequest request, User user) throws JOSEException {
        return jwtTokenCoder.createToken(request.getRemoteHost(), user.getId(), user.getDisplayName(), user.getRole());
    }

    public static class UnlinkRequest {

        @NotBlank
        String provider;


        public String getProvider() {
            return provider;
        }


        public void setProvider(String provider) {
            this.provider = provider;
        }

    }

    public static class Payload {

        @NotBlank
        String clientId;

        @NotBlank
        String redirectUri;

        @NotBlank
        String code;


        public String getClientId() {
            return clientId;
        }


        public String getRedirectUri() {
            return redirectUri;
        }


        public String getCode() {
            return code;
        }
    }
}