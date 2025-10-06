package org.pierce.bridge;

import io.netty.channel.*;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.pierce.JproxyProperties;
import org.pierce.LocalServer;
import org.pierce.MessageBridge;
import org.pierce.exception.DirectiveDisallowException;
import org.pierce.handler.DebugHandler;
import org.pierce.handler.JproxyHandler;
import org.pierce.handler.TlsClientHandlerBuilder;
import org.pierce.list.Directive;
import org.pierce.list.entity.ConnectType;
import org.pierce.list.entity.MessageWrap;
import org.pierce.list.entity.TryConnect;
import org.pierce.pool.NettyChannelPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.Queue;

public class ByteBufMessageBridge implements MessageBridge {


    public static final AttributeKey<Channel> LINK_IN = AttributeKey.valueOf("LINK_IN");

    public static final AttributeKey<String> TARGET_ADDRESS = AttributeKey.valueOf("TARGET_ADDRESS");

    public static final AttributeKey<Integer> TARGET_PORT = AttributeKey.valueOf("TARGET_PORT");

    public static String proxyAddress = JproxyProperties.getProperty("local-server.link-out.address");

    public static int proxyPort = Integer.parseInt(JproxyProperties.getProperty("local-server.remote-websocket-link-out.port"));

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
                        linkIn.attr(LINK_OUT).set(ctx.channel());
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

    final static NettyChannelPoolManager remoteLinkOut = new NettyChannelPoolManager(new ChannelInitializer<>() {
        @Override
        protected void initChannel(Channel channel) throws URISyntaxException {
            //log.info("proxy {}:{}", proxyAddress, proxyPort);
            if (JproxyProperties.booleanVal("tls-debug")) {
                channel.pipeline().addLast(new DebugHandler("tls-link-out"));
            }
            if (JproxyProperties.booleanVal("local-server.link-out.tls")) {
                channel.pipeline().addLast(TlsClientHandlerBuilder.getInstance().build(channel));
            }
            if (JproxyProperties.booleanVal("debug")) {
                channel.pipeline().addLast(new DebugHandler("byte-proxy-link-out-0"));
            }
            channel.pipeline().addLast(new JproxyHandler(new InetSocketAddress(proxyAddress, proxyPort)));
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
                        linkIn.attr(LINK_OUT).set(ctx.channel());
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
            LocalServer.getInstance().connectionTypeCheck.check(linkIn, targetAddress, targetPort).addListener(new GenericFutureListener<Future<? super ConnectType>>() {
                @Override
                public void operationComplete(Future<? super ConnectType> future) throws Exception {
                    if (future.isSuccess()) {
                        ConnectType connectType = (ConnectType) future.getNow();
                        if (connectType.getDirective() == Directive.DISALLOW_CONNECT || connectType.getDirective() == Directive.MISS) {
                            channelPromise.tryFailure(new DirectiveDisallowException("future.isSuccess()"));
                            return;
                        }

                        bridge0(connectType, connectType.getAddress(), targetPort, linkIn, channelPromise);
                    } else {
                        channelPromise.tryFailure(new RuntimeException("!future.isSuccess()"));
                    }
                }
            });


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

    public void bridge0(ConnectType connectType, String targetAddress, int targetPort, Channel linkIn, ChannelPromise channelPromise) {
        Future<Channel> channelFuture;
        if (connectType.getDirective() == Directive.FULL_CONNECT) {
            channelFuture = remoteLinkOut.acquire(targetAddress, targetPort);
        } else if (connectType.getDirective() == Directive.DIRECT_CONNECT) {
            channelFuture = localLinkOut.acquire(targetAddress, targetPort);
        } else {
            channelPromise.tryFailure(new RuntimeException("connectType.getDirective():" + connectType.getDirective()));
            return;
        }

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
