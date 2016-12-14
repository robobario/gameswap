package org.gameswap.application;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.gameswap.messaging.model.VerifyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

public class UserVerifier {

    private CloseableHttpClient httpClient;
    private WorkerConfiguration config;

    public UserVerifier(CloseableHttpClient httpClient, WorkerConfiguration configuration){
        this.httpClient = httpClient;
        this.config = configuration;
    }

    private static final Logger logger = LoggerFactory.getLogger(Worker.class);

    public boolean verify(VerifyRequest request){
        try {
            return checkCodeInBoardgameGeeksProfile(request);
        } catch (URISyntaxException e) {
            logger.error("failed", e);
            return false;
        }
    }

    private boolean checkCodeInBoardgameGeeksProfile(VerifyRequest request) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(config.getBggApiRootUrl() + "user");
        uriBuilder.addParameter("name", request.getBggUserName());
        HttpGet httpGet = new HttpGet(uriBuilder.build());
        logger.info(httpGet.getURI().toASCIIString());
        try(CloseableHttpResponse response = httpClient.execute(httpGet)){
            HttpEntity entity = response.getEntity();
            String body = responseToString(entity);
            logger.info(body);
            boolean containsCode = body.contains(request.getCode());
            EntityUtils.consume(entity);
            return containsCode;
        } catch (Exception e) {
            logger.error("failed to make http request", e);
            return false;
        }
    }

    private String responseToString(HttpEntity entity) throws IOException {
        byte[] bytes = ByteStreams.toByteArray(entity.getContent());
        return new String(bytes, Charsets.UTF_8);
    }

}
