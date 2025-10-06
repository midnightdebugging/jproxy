package org.pierce;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Promise;
import org.pierce.list.entity.ConnectType;

public interface ConnectionTypeCheck {
    void check(Channel channel, String targetHost, int targetPort, Promise<ConnectType> promise);

    Promise<ConnectType> check(Channel channel, String targetHost, int targetPort);
}
