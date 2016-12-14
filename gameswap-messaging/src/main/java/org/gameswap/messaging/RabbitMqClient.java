package org.gameswap.messaging;

import com.google.common.collect.ImmutableMap;

import com.rabbitmq.client.*;

import net.jodah.lyra.ConnectionOptions;
import net.jodah.lyra.Connections;
import net.jodah.lyra.config.Config;
import net.jodah.lyra.config.RecoveryPolicy;
import net.jodah.lyra.util.Duration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeoutException;

class RabbitMqClient {

    private final ConnectionOptions options;
    private final Config config;
    private final List<String> queues;
    private Channel channel;
    private ChannelAcker channelAcker;

    public RabbitMqClient(String address, List<String> queues) {
        this.queues = queues;
        config = new Config()
                .withRecoveryPolicy(new RecoveryPolicy()
                        .withBackoff(Duration.seconds(1), Duration.seconds(30))
                        .withMaxAttempts(20));
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try {
            connectionFactory.setUri(address);
        } catch (Exception e) {
            e.printStackTrace();
        }
        options = new ConnectionOptions(connectionFactory);
    }

    public void init() {
        try {
            Connection connection = Connections.create(options, config);
            channel = connection.createChannel(1);
            queues.forEach(this::declare);
            channelAcker = new ChannelAcker(channel);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private AMQP.Queue.DeclareOk declare(String queue) {
        try {
            return channel.queueDeclare(queue, true, false, false, ImmutableMap.of());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Result send(String queue, byte[] message) {
        try {
            channel.basicPublish("", queue, null, message);
            channel.waitForConfirmsOrDie(30000);
            return new Result(true, null);
        } catch (Exception e) {
            return new Result(false, e.getMessage());
        }
    }

    public void register(MessageReceiver receiver, String queue) {
        try {
            channel.basicConsume(queue, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    receiver.receive(channelAcker, body, envelope.getDeliveryTag());
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
