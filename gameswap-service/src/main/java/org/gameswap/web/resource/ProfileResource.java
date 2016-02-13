package org.gameswap.web.resource;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.LongParam;
import org.gameswap.model.Role;
import org.gameswap.model.User;
import org.gameswap.model.UserPrincipal;
import org.gameswap.persistance.UserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/profile")
public class ProfileResource {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileResource.class);

    private final UserDAO dao;

    public ProfileResource(UserDAO dao) {
        this.dao = dao;
    }


    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    @RolesAllowed(Role.USER)
    public User get(@PathParam("id") LongParam id, @Auth UserPrincipal userPrincipal) {
        LOG.info("user principal {}", userPrincipal);
        Optional<User> entity = dao.find(id.get());
        if (!entity.isPresent()) {
            throw new NotFoundException("User " + id + " not found");
        }
        User user = entity.get();
        LOG.info("Found user: {}", user);
        return user;
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    public User update(@PathParam("id") LongParam id, @Valid User entity) {
        Optional<User> ent = dao.find(id.get());
        if (!ent.isPresent()) {
            throw new NotFoundException("User " + id + " not found");
        }
        return dao.merge(entity);
    }
}
