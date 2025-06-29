package org.pierce;

import io.netty.channel.EventLoopGroup;

public interface JproxyServer {
    void start(EventLoopGroup eventLoopGroup);
}
