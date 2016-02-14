package org.gameswap.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

class JacksonMessageSender implements MessageSender {

    private final RabbitMqClient client;
    private ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(JacksonMessageSender.class);

    public JacksonMessageSender(RabbitMqClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    @Override
    public void init() {
        client.init();
    }

    @Override
    public <T> Result send(T object, String queue, Class<T> clazz) {
        try {
            return client.send(queue, objectMapper.writerFor(clazz).writeValueAsBytes(object));
        } catch (JsonProcessingException e) {
            return new Result(false, e.getMessage());
        }
    }

    @Override
    public <T> Function<T, Result> sender(String queue, Class<T> clazz) {
        ObjectWriter writer = objectMapper.writerFor(clazz);
        return obj -> {
            try {
                return client.send(queue, writer.writeValueAsBytes(obj));
            } catch (JsonProcessingException e) {
                return new Result(false, e.getMessage());
            }
        };
    }
}
