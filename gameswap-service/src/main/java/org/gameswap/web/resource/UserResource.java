package org.gameswap.web.resource;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.LongParam;
import org.gameswap.web.model.Role;
import org.gameswap.web.model.User;
import org.gameswap.persistance.UserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/users")
public class UserResource {

    private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);

    private final UserDAO dao;

    public UserResource(UserDAO dao) {
        this.dao = dao;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    @RolesAllowed(Role.ADMIN)
    public List<User> getAll() {
        return dao.findAll();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    @RolesAllowed(Role.USER)
    public User get(@PathParam("id") LongParam id) {
        Optional<User> entity = dao.find(id.get());
        if (!entity.isPresent()) {
            throw new NotFoundException("User " + id.get() + " not found");
        }
        return entity.get();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    @RolesAllowed(Role.USER)
    public User update(@PathParam("id") LongParam id, User entity) {
        Optional<User> ent = dao.find(id.get());
        if (!ent.isPresent()) {
            throw new NotFoundException("User " + id.get() + " not found");
        }
        return dao.merge(entity);
    }

    @DELETE
    @Path("{id}")
    @Timed
    @UnitOfWork
    @RolesAllowed(Role.USER)
    public void delete(@PathParam("id") LongParam id) {
        Optional<User> entity = dao.find(id.get());
        if (!entity.isPresent()) {
            throw new NotFoundException("User " + id.get() + " not found");
        }
        dao.delete(entity.get());
    }
}
