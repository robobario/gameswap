package org.gameswap.web.resource;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import org.gameswap.messaging.MessageSender;
import org.gameswap.messaging.Result;
import org.gameswap.web.authentication.OneTimePasswordMaker;
import org.gameswap.web.model.*;
import org.gameswap.messaging.model.VerifyRequest;
import org.gameswap.persistance.UserDAO;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.function.Function;


@Path("/verify")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class VerifyResource {

    private final Function<VerifyRequest, Result> messageSender;

    private final UserDAO userDAO;

    private final OneTimePasswordMaker oneTimePasswordMaker;

    public VerifyResource(MessageSender messageSender, UserDAO userDAO, OneTimePasswordMaker oneTimePasswordMaker) {
        this.userDAO = userDAO;
        this.oneTimePasswordMaker = oneTimePasswordMaker;
        this.messageSender = messageSender.sender("verify", VerifyRequest.class);
    }

    @POST
    @Path("/initiate")
    @Timed
    @UnitOfWork
    @RolesAllowed(Role.USER)
    public Response initiate(@Auth UserPrincipal userPrincipal, RequestVerification verify) {
        Optional<User> user = userDAO.findByName(userPrincipal.getUsername());
        if (user.isPresent()) {
            return getCodeAndRecord(verify, user.get());
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    private Response getCodeAndRecord(RequestVerification verify, User user) {
        String password = oneTimePasswordMaker.getOneTimePassword();
        user.setLastOneTimePassword(password);
        user.setBggUserName(verify.getBggUserName());
        userDAO.save(user);
        return Response.ok(new VerificationCode(password)).build();
    }

    @GET
    @Path("/attempt")
    @Timed
    @UnitOfWork(readOnly = true)
    @RolesAllowed(Role.USER)
    public Response attempt(@Auth UserPrincipal userPrincipal) {
        Optional<User> user = userDAO.findByName(userPrincipal.getUsername());
        if (user.isPresent()) {
            return sendVerificationRequest(user).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    private Response.ResponseBuilder sendVerificationRequest(Optional<User> user) {
        User present = user.get();
        if (present.getLastOneTimePassword() != null && present.getBggUserName() != null) {
            messageSender.apply(new VerifyRequest(present.getId(), present.getBggUserName(), present.getLastOneTimePassword()));
            return Response.ok();
        } else {
            return Response.status(Response.Status.NOT_FOUND);
        }
    }

}
