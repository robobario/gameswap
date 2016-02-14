package org.gameswap.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JacksonMessageSenderTest {

    @Test
    public void send() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        RabbitMqClient mock = mock(RabbitMqClient.class);
        when(mock.send("auth", objectMapper.writeValueAsBytes("thing"))).thenReturn(new Result(true, null));
        JacksonMessageSender messageSender = new JacksonMessageSender(mock, objectMapper);
        Result send = messageSender.send("thing", "auth", String.class);
        assertTrue(send.isSuccess());
    }

    @Test
    public void sender() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        RabbitMqClient mock = mock(RabbitMqClient.class);
        when(mock.send("auth", objectMapper.writeValueAsBytes("thing"))).thenReturn(new Result(true, null));
        JacksonMessageSender messageSender = new JacksonMessageSender(mock, objectMapper);
        Function<String, Result> function = messageSender.sender("auth", String.class);
        Result result = function.apply("thing");
        assertTrue(result.isSuccess());
    }
}