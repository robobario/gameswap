package org.gameswap.web.resource;

import com.codahale.metrics.annotation.Timed;
import org.gameswap.model.Role;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path("/v1/test")
public class TestResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Timed
    @RolesAllowed(Role.USER)
    public String get() {
        return "string";
    }

}
