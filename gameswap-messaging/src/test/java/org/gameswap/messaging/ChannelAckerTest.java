package org.gameswap.messaging;

import com.rabbitmq.client.Channel;

import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ChannelAckerTest {

    @Test
    public void ack() throws IOException {
        Channel mock = mock(Channel.class);
        ChannelAcker acker = new ChannelAcker(mock);
        acker.ack(6L);
        verify(mock).basicAck(6L, false);
    }

    @Test
    public void nack() throws IOException {
        Channel mock = mock(Channel.class);
        ChannelAcker acker = new ChannelAcker(mock);
        acker.nack(6L);
        verify(mock).basicNack(6L, false, true);
    }

    @Test(expected = RuntimeException.class)
    public void ackFail() throws IOException {
        Channel mock = mock(Channel.class);
        Mockito.doThrow(new IOException()).when(mock).basicAck(6L, false);
        ChannelAcker acker = new ChannelAcker(mock);
        acker.ack(6L);
    }

    @Test(expected = RuntimeException.class)
    public void nackFail() throws IOException {
        Channel mock = mock(Channel.class);
        Mockito.doThrow(new IOException()).when(mock).basicNack(6L, false, true);
        ChannelAcker acker = new ChannelAcker(mock);
        acker.nack(6L);
    }
}