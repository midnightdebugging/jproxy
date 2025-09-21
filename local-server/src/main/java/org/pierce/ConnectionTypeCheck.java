package org.pierce;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Promise;
import org.pierce.entity.ConnectType;

public interface ConnectionTypeCheck {
    void check(Channel channel, String targetHost, int targetPort, Promise<ConnectType> promise);
}
