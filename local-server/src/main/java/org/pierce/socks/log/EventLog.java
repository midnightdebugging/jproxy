package org.pierce.socks.log;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public interface EventLog {


    public void debug(ChannelHandlerContext ctx, String info);

    public void info(ChannelHandlerContext ctx, String info);

    public void error(ChannelHandlerContext ctx, String info);

    public void error(ChannelHandlerContext ctx, String info,Throwable cause);

    public void debug(Channel channel, String info);

    public void info(Channel channel, String info);

    public void error(Channel channel, String info);

    public void error(Channel channel, String info,Throwable cause);
}
