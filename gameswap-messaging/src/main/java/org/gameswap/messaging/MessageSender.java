package org.gameswap.messaging;

import java.util.function.Function;

public interface MessageSender {
    void init();

    <T> Result send(T object, String queue, Class<T> clazz);

    <T> Function<T, Result> sender(String queue, Class<T> clazz);
}
