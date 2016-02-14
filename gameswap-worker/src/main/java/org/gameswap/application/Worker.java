package org.gameswap.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.*;
import io.dropwizard.validation.BaseValidator;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.gameswap.messaging.MessageConsumer;
import org.gameswap.messaging.MessageSender;
import org.gameswap.messaging.Messagers;
import org.gameswap.messaging.Result;
import org.gameswap.messaging.model.VerifyRequest;
import org.gameswap.messaging.model.VerifyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Worker {

    private MessageSender sender;

    private final CloseableHttpClient httpClient;
    private final UserVerifier userVerifier;
    private static final Logger logger = LoggerFactory.getLogger(Worker.class);
    private WorkerConfiguration config;

    public Worker(String[] args) {
        try {
            if(args.length == 0){
                throw new IllegalArgumentException("expect at least one argument");
            }
            ObjectMapper objectMapper = new ObjectMapper();
            config = loadConfig(args[0], objectMapper);
            MessageConsumer consumer = Messagers.consumer(config.getRabbitMqReceiveUrl(), objectMapper);
            sender = Messagers.sender(config.getRabbitMqReceiveUrl(), objectMapper);
            sender.init();
            consumer.init();
            consumer.register(this::onVerify, VerifyRequest.class, "verify");
            httpClient = HttpClients.createDefault();
            userVerifier = new UserVerifier(httpClient, config);
        }catch (Exception e){
            throw  new ExceptionInInitializerError(e);
        }
    }

    public static void main(String[] args) {
        try {
            new Worker(args);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private Result onVerify(VerifyRequest request) {
        logger.info("request verification recieved: {}", request);
        boolean result = userVerifier.verify(request);
        sender.send(new VerifyResponse(request.getAccountId(), request.getBggUserName(), result),"verifyResponse", VerifyResponse.class);
        logger.info("request verification result: {}", result);
        return new Result(true, null);
    }


    public static WorkerConfiguration loadConfig(String arg, ObjectMapper objectMapper) throws IOException, ConfigurationException {
        ConfigurationFactory<WorkerConfiguration> dw = new ConfigurationFactory<>(WorkerConfiguration.class, BaseValidator.newValidator(), objectMapper, "dw");
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor(false);
        SubstitutingSourceProvider provider = new SubstitutingSourceProvider(new FileConfigurationSourceProvider(), substitutor);
        return dw.build(provider, arg);
    }
}
