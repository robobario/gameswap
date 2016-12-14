package org.gameswap.messaging;

public interface MessageConsumer {
    void init();

    <T> void register(Processor<T> handler, Class<T> clazz, String queue);
}
