package org.gameswap.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.jersey.params.BooleanParam;
import io.dropwizard.jersey.params.DateTimeParam;
import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.LongParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/user")
public class UserResource {

    private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);

    
    @Path("/user/{userid}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed 
    public String getuser(
        
        @PathParam("userid") String userid
        ) {
        StringBuilder sb = new StringBuilder();
        sb.append("Received parameters:\n");
        
        sb.append("userid=");
        sb.append(userid);
        sb.append("\n");
        
        return sb.toString();
    }
    
    @Path("/user/{userid}")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Timed 
    public String putUser(
        
        @PathParam("userid") String userid
        ) {
        StringBuilder sb = new StringBuilder();
        sb.append("Received parameters:\n");
        
        sb.append("userid=");
        sb.append(userid);
        sb.append("\n");
        
        return sb.toString();
    }
    
    @Path("/user/{userid}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Timed 
    public String newUser(
        
        @PathParam("userid") String userid
        ) {
        StringBuilder sb = new StringBuilder();
        sb.append("Received parameters:\n");
        
        sb.append("userid=");
        sb.append(userid);
        sb.append("\n");
        
        return sb.toString();
    }
    
    @Path("/user/{userid}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Timed 
    public String deleteUser(
        
        @PathParam("userid") String userid
        ) {
        StringBuilder sb = new StringBuilder();
        sb.append("Received parameters:\n");
        
        sb.append("userid=");
        sb.append(userid);
        sb.append("\n");
        
        return sb.toString();
    }
    
}
