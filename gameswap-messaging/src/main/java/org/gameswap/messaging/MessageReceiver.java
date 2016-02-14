package org.gameswap.messaging;

interface MessageReceiver {

    void receive(ChannelAcker acker, byte[] bytes, long deliveryTag);
}
