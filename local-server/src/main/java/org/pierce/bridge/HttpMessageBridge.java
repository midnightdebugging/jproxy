package org.pierce.bridge;

import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.pierce.JproxyProperties;
import org.pierce.LocalServer;
import org.pierce.MessageBridge;
import org.pierce.UtilTools;
import org.pierce.entity.ProtocolInfo;
import org.pierce.exception.DirectiveDisallowException;
import org.pierce.handler.DebugHandler;
import org.pierce.handler.JproxyHandler;
import org.pierce.handler.TlsClientHandlerBuilder;
import org.pierce.list.Directive;
import org.pierce.list.entity.ConnectType;
import org.pierce.list.entity.MessageWrap;
import org.pierce.pool.NettyChannelPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class HttpMessageBridge implements MessageBridge {


    public static final AttributeKey<Channel> LINK_IN = AttributeKey.valueOf("LINK_IN");

    private static final Logger log = LoggerFactory.getLogger(HttpMessageBridge.class);

    public static String proxyAddress = JproxyProperties.getProperty("local-server.link-out.address");

    public static int proxyPort = Integer.parseInt(JproxyProperties.getProperty("local-server.remote-websocket-link-out.port"));

    final static NettyChannelPoolManager localLinkOut = new NettyChannelPoolManager(new ChannelInitializer<>() {
        @Override
        protected void initChannel(Channel channel) {
            if (JproxyProperties.booleanVal("debug")) {
                channel.pipeline().addLast(new DebugHandler("http-msg-link-out"));
            }
            channel.pipeline().addLast(new HttpClientCodec());
            channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    log.info("{}:{}", UtilTools.formatChannelInfo(ctx), msg.getClass());
                    if (msg instanceof LastHttpContent) {
                        //需要完全响应完之后才能释放
                        NettyChannelPoolManager.release(channel);
                    }
                    Channel linkIn = ctx.channel().attr(LINK_IN).get();
                    if (linkIn != null) {
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
                }
            });
        }
    });

    final static NettyChannelPoolManager remoteLinkOut = new NettyChannelPoolManager(new ChannelInitializer<>() {
        @Override
        protected void initChannel(Channel channel) {
            log.info("proxy {}:{}", proxyAddress, proxyPort);
            if (JproxyProperties.booleanVal("tls-debug")) {
                channel.pipeline().addLast(new DebugHandler("tls-link-out"));
            }
            if (JproxyProperties.booleanVal("local-server.link-out.tls")) {
                channel.pipeline().addLast(TlsClientHandlerBuilder.getInstance().build(channel));
            }
            if (JproxyProperties.booleanVal("debug")) {
                channel.pipeline().addLast(new DebugHandler("http-msg-proxy-link-out"));
            }
            channel.pipeline().addLast(new JproxyHandler(new InetSocketAddress(proxyAddress, proxyPort)));
            channel.pipeline().addLast(new HttpClientCodec());
            channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    log.info("{}:{}", UtilTools.formatChannelInfo(ctx), msg.getClass());
                    if (msg instanceof LastHttpContent) {
                        //需要完全响应完之后才能释放
                        NettyChannelPoolManager.release(channel);
                    }
                    Channel linkIn = ctx.channel().attr(LINK_IN).get();
                    if (linkIn != null) {
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
                }
            });
        }
    });

    Channel lastLinkOut = null;

    final Queue<MessageWrap> queue = new LinkedList<>();

    Throwable cause = null;

    @Override
    public ChannelPromise bridge(Channel linkIn, Object message) {
        ChannelPromise channelPromise = linkIn.newPromise();

        if (message instanceof HttpRequest request) {
            //解析获取目标地址
            String uri = request.uri();
            ProtocolInfo protocolInfo = UtilTools.parseProtocolInfo(uri);


            String targetAddress = protocolInfo.getHostAddress();
            int targetPort = protocolInfo.getPort();

            HttpRequest newHttpRequest = new DefaultHttpRequest(request.protocolVersion(), request.method(), protocolInfo.getPath());

            for (Map.Entry<String, String> entry : request.headers()) {
                newHttpRequest.headers().set(entry.getKey(), entry.getValue());
            }
            queue.add(new MessageWrap(channelPromise, message));

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
                        channelPromise.tryFailure(new RuntimeException("future.isSuccess()"));
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
                lastLinkOut.writeAndFlush(message).addListener(new GenericFutureListener<>() {
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
            if (message instanceof LastHttpContent) {
                //已经完成一个消息的发送
                lastLinkOut = null;
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
            }
        });
    }
}
