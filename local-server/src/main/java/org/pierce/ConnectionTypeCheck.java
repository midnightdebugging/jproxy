package org.pierce;

import io.netty.channel.EventLoop;
import io.netty.util.concurrent.Promise;
import org.pierce.entity.ConnectType;

public interface ConnectionTypeCheck {
    void check(EventLoop eventLoop, String targetHost, Promise<ConnectType> promise);
}
