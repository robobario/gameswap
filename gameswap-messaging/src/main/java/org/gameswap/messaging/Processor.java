package org.gameswap.messaging;

public interface Processor<T> {

    public Result handle(T it);

}
