package org.gameswap.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.LongParam;
import org.gameswap.daos.UserDAO;
import org.gameswap.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/v1/test")
public class TestResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Timed
    public String get() {
        return "string";
    }

}
