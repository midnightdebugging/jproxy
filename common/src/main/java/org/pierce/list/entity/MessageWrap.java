package org.pierce.list.entity;

import io.netty.channel.ChannelPromise;

public class MessageWrap {

    ChannelPromise promise;

    Object message;

    public ChannelPromise promise() {
        return promise;
    }

    public void setPromise(ChannelPromise promise) {
        this.promise = promise;
    }

    public Object message() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public MessageWrap(ChannelPromise promise, Object message) {
        this.promise = promise;
        this.message = message;
    }
}
