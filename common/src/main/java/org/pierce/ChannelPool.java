package org.pierce;

import io.netty.channel.Channel;

import java.util.concurrent.CompletableFuture;

public interface ChannelPool {
    CompletableFuture<Channel> acquire();

    void release(Channel channel);

    void close();
}
