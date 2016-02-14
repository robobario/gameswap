package org.gameswap.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Messagers {

    private static final Logger logger = LoggerFactory.getLogger(Messagers.class);

    public static MessageSender sender(String address, ObjectMapper objectMapper){
        return new JacksonMessageSender(new RabbitMqClient(address, ImmutableList.of("verify","verifyResponse")), objectMapper);
    }
    public static MessageConsumer consumer(String address, ObjectMapper objectMapper){
        return new JacksonMessageConsumer(new RabbitMqClient(address, ImmutableList.of("verify","verifyResponse")), objectMapper);
    }
}
