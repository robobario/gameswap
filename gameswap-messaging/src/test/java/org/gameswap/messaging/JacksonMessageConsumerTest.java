package org.gameswap.messaging;

import com.google.common.base.Charsets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class JacksonMessageConsumerTest {

    public static final long DELIVERY_TAG = 5L;
    public static final String INVALID_JSON = "{";

    @Test
    public void successfulDecodeAcks() throws InterruptedException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        RabbitMqClient mock = mock(RabbitMqClient.class);
        JacksonMessageConsumer consumer = new JacksonMessageConsumer(mock, objectMapper);
        AtomicReference<String> ref = new AtomicReference<>(null);
        consumer.register(it -> {
            ref.set(it);
            return new Result(true, null);
        }, String.class, "auth");
        ArgumentCaptor<MessageReceiver> captor = ArgumentCaptor.forClass(MessageReceiver.class);
        verify(mock).register(captor.capture(), eq("auth"));
        MessageReceiver receiver = captor.getValue();
        ChannelAcker mockAcker = mock(ChannelAcker.class);
        receiver.receive(mockAcker, objectMapper.writeValueAsBytes("hello"), DELIVERY_TAG);
        assertEquals(ref.get(), "hello");
        verify(mockAcker).ack(DELIVERY_TAG);
        verifyNoMoreInteractions(mockAcker);
    }

    @Test
    public void badJsonNacks() throws InterruptedException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        RabbitMqClient mock = mock(RabbitMqClient.class);
        JacksonMessageConsumer consumer = new JacksonMessageConsumer(mock, objectMapper);
        consumer.register(it -> new Result(true, null), String.class, "auth");
        ArgumentCaptor<MessageReceiver> captor = ArgumentCaptor.forClass(MessageReceiver.class);
        verify(mock).register(captor.capture(), eq("auth"));
        MessageReceiver receiver = captor.getValue();
        ChannelAcker mockAcker = mock(ChannelAcker.class);
        receiver.receive(mockAcker, INVALID_JSON.getBytes(Charsets.UTF_8), DELIVERY_TAG);
        verify(mockAcker).nack(DELIVERY_TAG);
        verifyNoMoreInteractions(mockAcker);
    }

    @Test
    public void runtimeExceptionNacks() throws InterruptedException, JsonProcessingException {
        ChannelAcker mockAcker = receiveMessageWithProcessor(it -> {
            throw new RuntimeException();
        });
        verify(mockAcker).nack(DELIVERY_TAG);
        verifyNoMoreInteractions(mockAcker);
    }

    @Test
    public void failedResultNacks() throws InterruptedException, JsonProcessingException {
        ChannelAcker mockAcker = receiveMessageWithProcessor(it -> new Result(false, "error msg"));
        verify(mockAcker).nack(DELIVERY_TAG);
        verifyNoMoreInteractions(mockAcker);
    }

    private ChannelAcker receiveMessageWithProcessor(Processor<String> stringProcessor) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        RabbitMqClient mock = mock(RabbitMqClient.class);
        JacksonMessageConsumer consumer = new JacksonMessageConsumer(mock, objectMapper);
        consumer.register(stringProcessor, String.class, "auth");
        ArgumentCaptor<MessageReceiver> captor = ArgumentCaptor.forClass(MessageReceiver.class);
        verify(mock).register(captor.capture(), eq("auth"));
        MessageReceiver receiver = captor.getValue();
        ChannelAcker mockAcker = mock(ChannelAcker.class);
        receiver.receive(mockAcker, objectMapper.writeValueAsBytes("hello"), DELIVERY_TAG);
        return mockAcker;
    }

}