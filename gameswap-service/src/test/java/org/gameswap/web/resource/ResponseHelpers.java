package org.gameswap.web.resource;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ResponseHelpers {
    public JsonElement getJsonResponse(Response response) throws IOException {
        String stringResponse = getStringResponse(response);
        return new JsonParser().parse(stringResponse);
    }

    public String getStringResponse(Response response) throws IOException {
        InputStreamReader stream = new InputStreamReader((ByteArrayInputStream) response.getEntity(), Charsets.UTF_8);
        return CharStreams.toString(stream);
    }
}