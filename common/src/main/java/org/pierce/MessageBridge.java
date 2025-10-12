package org.pierce;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;

public interface MessageBridge {

    AttributeKey<Channel> LINK_OUT = AttributeKey.valueOf("LINK_OUT");

    ChannelPromise bridge(Channel linkIn, Object message);
}
