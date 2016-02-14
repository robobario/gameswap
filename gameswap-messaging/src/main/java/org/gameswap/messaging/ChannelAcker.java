package org.gameswap.messaging;

import com.rabbitmq.client.Channel;

import java.io.IOException;

class ChannelAcker {
    private Channel channel;

    public ChannelAcker(Channel channel) {
        this.channel = channel;
    }

    public void nack(long deliveryTag) {
        try {
            channel.basicNack(deliveryTag, false, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void ack(long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
