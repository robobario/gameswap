package org.gameswap.web.resource;

import com.google.common.base.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;

import org.apache.commons.lang3.StringUtils;
import org.gameswap.application.GameswapConfiguration;
import org.gameswap.model.Token;
import org.gameswap.model.User;
import org.gameswap.model.User.Provider;
import org.gameswap.persistance.UserDAO;
import org.gameswap.security.PasswordService;
import org.gameswap.web.authentication.AuthUtils;
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

    public static final String CLIENT_ID_KEY = "client_id", REDIRECT_URI_KEY = "redirect_uri", CLIENT_SECRET = "client_secret", CODE_KEY = "code",
            GRANT_TYPE_KEY = "grant_type", AUTH_CODE = "authorization_code";
    public static final String CONFLICT_MSG = "There is already a %s account that belongs to you", NOT_FOUND_MSG = "User not found",
            LOGING_ERROR_MSG = "Wrong email and/or password", UNLINK_ERROR_MSG = "Could not unlink %s account because it is your only sign-in method";
    public static final ObjectMapper MAPPER = new ObjectMapper();
    private final Client client;
    private final UserDAO dao;
    private final GameswapConfiguration secrets;


    public AuthResource(final Client client, final UserDAO dao, final GameswapConfiguration secrets) {
        this.client = client;
        this.dao = dao;
        this.secrets = secrets;
    }


    @POST
    @Path("login")
    @UnitOfWork
    public Response login(@Valid final User user, @Context final HttpServletRequest request) throws JOSEException {
        final Optional<User> foundUser = dao.findByName(user.getUsername());
        if (foundUser.isPresent() && PasswordService.checkPassword(user.getPassword(), foundUser.get().getPassword())) {
            final Token token = AuthUtils.createToken(request.getRemoteHost(), foundUser.get().getId());
            return Response.ok().entity(token).build();
        }
        return Response.status(Status.UNAUTHORIZED).entity(new ErrorMessage(LOGING_ERROR_MSG)).build();
    }


    @POST
    @Path("signup")
    @UnitOfWork
    public Response signup(@Valid final User user, @Context final HttpServletRequest request) throws JOSEException {
        Optional<User> existing = dao.findByName(user.getUsername());
        if (existing.isPresent()) {
            return Response.status(Status.UNAUTHORIZED).entity(new ErrorMessage("account with name already exists")).build();
        }
        user.setPassword(PasswordService.hashPassword(user.getPassword()));
        final User savedUser = dao.save(user);
        final Token token = AuthUtils.createToken(request.getRemoteHost(), savedUser.getId());
        return Response.status(Status.CREATED).entity(token).build();
    }


    @POST
    @Path("google")
    @UnitOfWork
    public Response loginGoogle(@Valid final Payload payload, @Context final HttpServletRequest request) throws JOSEException, ParseException,
            IOException {
        final String accessTokenUrl = "https://accounts.google.com/o/oauth2/token";
        final String peopleApiUrl = "https://www.googleapis.com/plus/v1/people/me/openIdConnect";
        Response response;

        // Step 1. Exchange authorization code for access token.
        final MultivaluedMap<String, String> accessData = new MultivaluedHashMap<>();
        accessData.add(CLIENT_ID_KEY, payload.getClientId());
        accessData.add(REDIRECT_URI_KEY, payload.getRedirectUri());
        accessData.add(CLIENT_SECRET, secrets.getGoogleSecret());
        accessData.add(CODE_KEY, payload.getCode());
        accessData.add(GRANT_TYPE_KEY, AUTH_CODE);
        response = client.target(accessTokenUrl).request().post(Entity.form(accessData));
        accessData.clear();

        // Step 2. Retrieve profile information about the current user.
        final String accessToken = (String) getResponseEntity(response).get("access_token");
        response = client.target(peopleApiUrl).request("text/plain").header(AuthUtils.AUTH_HEADER_KEY, String.format("Bearer %s", accessToken)).get();
        final Map<String, Object> userInfo = getResponseEntity(response);

        // Step 3. Process the authenticated the user.
        return processUser(request, Provider.GOOGLE, userInfo.get("sub").toString(), userInfo.get("name").toString());
    }


    @POST
    @Path("unlink/")
    @UnitOfWork
    public Response unlink(@Valid final UnlinkRequest unlinkRequest,
                           @Context final HttpServletRequest request) throws ParseException, IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException, SecurityException, JOSEException {
        final String subject = AuthUtils.getSubject(request.getHeader(AuthUtils.AUTH_HEADER_KEY));
        final Optional<User> foundUser = dao.findById(Long.parseLong(subject));

        String provider = unlinkRequest.provider;

        if (!foundUser.isPresent()) {
            return Response.status(Status.NOT_FOUND).entity(new ErrorMessage(NOT_FOUND_MSG)).build();
        }

        final User userToUnlink = foundUser.get();

        // check that the user is not trying to unlink the only sign-in method
        if (userToUnlink.getSignInMethodCount() == 1) {
            return Response.status(Status.BAD_REQUEST).entity(new ErrorMessage(String.format(UNLINK_ERROR_MSG, provider))).build();
        }

        try {
            userToUnlink.setProviderId(Provider.valueOf(provider.toUpperCase()), null);
        } catch (final IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        dao.save(userToUnlink);

        return Response.ok().build();
    }

    /*
     * Helper methods
     */
    private Map<String, Object> getResponseEntity(final Response response) throws IOException {
        return MAPPER.readValue(response.readEntity(String.class), new TypeReference<Map<String, Object>>() {
        });
    }

    private Response processUser(final HttpServletRequest request, final Provider provider, final String id,
                                 final String displayName) throws ParseException, JOSEException {
        final Optional<User> user = dao.findByProvider(provider, id);

        // Step 3a. If user is already signed in then link accounts.
        User userToSave;
        final String authHeader = request.getHeader(AuthUtils.AUTH_HEADER_KEY);
        if (StringUtils.isNotBlank(authHeader)) {
            if (user.isPresent()) {
                return Response.status(Status.CONFLICT).entity(new ErrorMessage(String.format(CONFLICT_MSG, provider.capitalize()))).build();
            }

            final String subject = AuthUtils.getSubject(authHeader);
            final Optional<User> foundUser = dao.findById(Long.parseLong(subject));
            if (!foundUser.isPresent()) {
                return Response.status(Status.NOT_FOUND).entity(new ErrorMessage(NOT_FOUND_MSG)).build();
            }

            userToSave = foundUser.get();
            userToSave.setProviderId(provider, id);
            if (userToSave.getDisplayName() == null) {
                userToSave.setDisplayName(displayName);
            }
            userToSave = dao.save(userToSave);
        } else {
            // Step 3b. Create a new user account or return an existing one.
            if (user.isPresent()) {
                userToSave = user.get();
            } else {
                userToSave = new User();
                userToSave.setProviderId(provider, id);
                userToSave.setDisplayName(displayName);
                userToSave = dao.save(userToSave);
            }
        }

        final Token token = AuthUtils.createToken(request.getRemoteHost(), userToSave.getId());
        return Response.ok().entity(token).build();
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

    /*
     * Inner classes for entity wrappers
     */
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