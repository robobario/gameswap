package org.gameswap.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JacksonMessageConsumer implements MessageConsumer {

    private final RabbitMqClient client;
    private ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(JacksonMessageConsumer.class);

    public JacksonMessageConsumer(RabbitMqClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    @Override
    public void init() {
        client.init();
    }

    @Override
    public <T> void register(Processor<T> handler, Class<T> clazz, String queue) {
        client.register((acker, bytes, deliveryTag) -> handle(bytes, clazz, handler, deliveryTag, acker), queue);
    }

    private <T> void handle(byte[] body, Class<T> clazz, Processor<T> handler, long deliveryTag, ChannelAcker channel) {
        try {
            ObjectReader objectReader = objectMapper.reader().forType(clazz);
            Result handle = handler.handle(objectReader.readValue(body));
            if (handle.isSuccess()) {
                channel.ack(deliveryTag);
            } else {
                channel.nack(deliveryTag);
                logger.debug("processor was not successful {}", handle.getErrorMessage());
            }
        } catch (Exception e) {
            channel.nack(deliveryTag);
            logger.debug("error during process", e);
        }
    }
}
