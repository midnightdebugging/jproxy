package org.pierce.bridge;

import io.netty.channel.*;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.pierce.JproxyProperties;
import org.pierce.MessageBridge;
import org.pierce.handler.DebugHandler;
import org.pierce.list.entity.MessageWrap;
import org.pierce.list.entity.TryConnect;
import org.pierce.pool.NettyChannelPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;

public class ByteBufMessageBridge implements MessageBridge {


    public static final AttributeKey<Channel> LINK_IN = AttributeKey.valueOf("LINK_IN");

    public static final AttributeKey<String> TARGET_ADDRESS = AttributeKey.valueOf("TARGET_ADDRESS");

    public static final AttributeKey<Integer> TARGET_PORT = AttributeKey.valueOf("TARGET_PORT");


    private static final Logger log = LoggerFactory.getLogger(ByteBufMessageBridge.class);

    final static NettyChannelPoolManager localLinkOut = new NettyChannelPoolManager(new ChannelInitializer<>() {
        @Override
        protected void initChannel(Channel channel) {

            if (JproxyProperties.booleanVal("debug")) {
                channel.pipeline().addLast(new DebugHandler("byte-link-out"));
            }
            channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    //log.info("{}:{}", UtilTools.formatChannelInfo(ctx), msg.getClass());
                    Channel linkIn = ctx.channel().attr(LINK_IN).get();
                    if (linkIn != null) {
                        if (!linkIn.isActive()) {
                            //级联关闭
                            ctx.channel().close();
                            return;
                        }
                        linkIn.writeAndFlush(msg);
                    }
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                    log.error("Throwable", cause);
                    if (ctx.channel().isActive()) {
                        ctx.channel().close();
                    }
                }

                @Override
                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                    log.info("channelInactive");
                    NettyChannelPoolManager.release(channel);
                    Channel linkIn = ctx.channel().attr(LINK_IN).get();
                    //级联关闭
                    if (linkIn != null) {
                        linkIn.close();
                    }

                }
            });
        }
    });

    Channel lastLinkOut = null;

    final Queue<MessageWrap> queue = new LinkedList<>();

    int status = 0;

    Throwable cause = null;

    @Override
    public ChannelPromise bridge(Channel linkIn, Object message) {
        ChannelPromise channelPromise = linkIn.newPromise();
        if (message instanceof TryConnect) {
            status = 1;
            String targetAddress = linkIn.attr(TARGET_ADDRESS).get();
            int targetPort = linkIn.attr(TARGET_PORT).get();
            queue.add(new MessageWrap(channelPromise, message));
            bridge0(targetAddress, targetPort, linkIn, channelPromise);

            return channelPromise;
        }
        synchronized (queue) {
            if (cause != null) {
                channelPromise.tryFailure(cause);
            } else if (lastLinkOut == null) {
                queue.add(new MessageWrap(channelPromise, message));
            } else {
                lastLinkOut.writeAndFlush(message).addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        if (future.isSuccess()) {
                            channelPromise.trySuccess();
                        } else {
                            channelPromise.tryFailure(future.cause());
                        }
                    }
                });
            }
        }


        return channelPromise;
    }

    public void bridge0(String targetAddress, int targetPort, Channel linkIn, ChannelPromise channelPromise) {
        Future<Channel> channelFuture = localLinkOut.acquire(targetAddress, targetPort);

        channelFuture.addListener((GenericFutureListener<Future<Channel>>) future -> {
            if (future.isSuccess()) {
                Channel channel = future.getNow();
                channel.attr(LINK_IN).set(linkIn);
                channelPromise.trySuccess();

                synchronized (queue) {
                    while (!queue.isEmpty()) {
                        MessageWrap messageWrap = queue.remove();
                        channel.writeAndFlush(messageWrap.message()).addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {
                                if (future.isSuccess()) {
                                    messageWrap.promise().trySuccess();
                                } else {
                                    messageWrap.promise().tryFailure(future.cause());
                                }
                            }
                        });
                    }
                }

                lastLinkOut = channel;
                return;
            } else {
                channelPromise.tryFailure(future.cause());
                cause = future.cause();
                synchronized (queue) {

                    while (!queue.isEmpty()) {
                        MessageWrap messageWrap = queue.remove();
                        messageWrap.promise().tryFailure(future.cause());
                    }
                }
            }
        });

    }
}
