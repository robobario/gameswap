package org.gameswap.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/test")
@Produces(MediaType.APPLICATION_JSON)
public class TestResource {

    private final ObjectMapper mapper;


    public TestResource(ObjectMapper objectMapper) {
        this.mapper = objectMapper;
    }


    @GET
    public ObjectNode getLatest() {
        return toJson();
    }


    private ObjectNode toJson() {
        ObjectNode node = mapper.createObjectNode();
        node.put("Hello,", "world!");
        return node;
    }
}
