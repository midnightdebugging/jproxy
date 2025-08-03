package org.pierce.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.util.concurrent.*;
import org.pierce.DomainQuery;
import org.pierce.JproxyProperties;
import org.pierce.UtilTools;
import org.pierce.codec.*;
import org.pierce.handler.DebugHandler;
import org.pierce.handler.TlsClientHandlerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class JproxyDomainQuery implements DomainQuery {


    private static final Logger log = LoggerFactory.getLogger(JproxyDomainQuery.class);

    private static final Map<String, List<Promise<List<String>>>> taskIndex = new HashMap<>();

    private static Channel channel;

    private static final Object channelObject = new Object();

    private final EventLoopGroup eventLoopGroup;

    public final static String remoteAddress = JproxyProperties.getProperty("local-server.link-out.address");

    public final static int remotePort = Integer.parseInt(JproxyProperties.getProperty("local-server.remote-websocket-link-out.port"));

    public JproxyDomainQuery(EventLoopGroup eventLoopGroup) {
        this.eventLoopGroup = eventLoopGroup;
    }

    @Override
    public void query(String domain, Promise<List<String>> promise) throws ExecutionException, InterruptedException, URISyntaxException {
        synchronized (taskIndex) {
            if (!taskIndex.containsKey(domain)) {
                taskIndex.put(domain, new ArrayList<>());
            }
            taskIndex.get(domain).add(promise);
        }
        synchronized (channelObject) {
            if (channel == null) {
                channel = newChannel();
            }
            if (channel != null && !channel.isActive()) {
                channel = newChannel();
            }
            SocksCommandDNSRequest request = new SocksCommandDNSRequest();
            request.setDomain(domain);
            channel.writeAndFlush(request);
        }
    }

    public Channel newChannel() throws InterruptedException, ExecutionException, URISyntaxException {
        return newChannel0().await().get();
    }

    public Promise<Channel> newChannel0() throws URISyntaxException {

        String url = JproxyProperties.evaluate("${local-server.link-out.address}:${local-server.remote-websocket-link-out.port}/${local-server.link-out.websocket-path}");
        if (JproxyProperties.booleanVal("local-server.link-out.tls")) {
            url = "wss://" + url;
        } else {
            url = "ws://" + url;
        }
        URI uri = new URI(url);
        final WebSocketClientHandler handler =
                new WebSocketClientHandler(
                        WebSocketClientHandshakerFactory.newHandshaker(
                                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders() {
                                    {
                                        add("WORK_TYPE", "02");
                                    }
                                }));

        EventExecutor executor = ImmediateEventExecutor.INSTANCE;
        Promise<Channel> channelPromise = executor.newPromise();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                if (JproxyProperties.booleanVal("tls-debug")) {
                    ch.pipeline().addLast(new DebugHandler("tls-link-out"));
                }
                if (JproxyProperties.booleanVal("local-server.link-out.tls")) {
                    ch.pipeline().addLast(TlsClientHandlerBuilder.getInstance().build(ch));
                }
                ch.pipeline().addLast(new DebugHandler("link-in"));
                ch.pipeline().addLast(new HttpClientCodec());
                ch.pipeline().addLast(new HttpObjectAggregator(8192));
                ch.pipeline().addLast(new WebSocketClientCompressionHandler(8192));
                ch.pipeline().addLast(handler);
                ch.pipeline().addLast(new SocksCommandDuplexHandler());
                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext linkOutCtx, Object msg) throws Exception {
                        log.info("yyy:{}", msg);
                        if (msg instanceof SocksCommandDNSResponse) {
                            SocksCommandDNSResponse response = (SocksCommandDNSResponse) msg;

                            synchronized (taskIndex) {
                                if (taskIndex.containsKey(response.getDomain())) {
                                    List<Promise<List<String>>> list = taskIndex.get(response.getDomain());


                                    for (Promise<List<String>> element : list) {
                                        if (response.getCode() == SocksCommandResponseCode.SUCCESS) {
                                            element.trySuccess(response.getIpList());
                                        } else {
                                            element.tryFailure(new RuntimeException(String.format("response.getCode():%s", response.getCode())));
                                        }

                                    }
                                    taskIndex.remove(response.getDomain());
                                }
                            }

                        }
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {

                        log.error("{}:", UtilTools.formatChannelInfo(ctx), throwable);
                        ctx.channel().close();
                        ctx.close();
                    }

                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        log.info("666");

                    }
                });
            }
        });

        log.info("connect {}:{}", remoteAddress, remotePort);
        bootstrap.connect(remoteAddress, remotePort).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    log.info("connect {}:{} future.isSuccess()", remoteAddress, remotePort);
                    //channelPromise.trySuccess(future.channel());
                    handler.handshakeFuture().addListener(future1 -> {
                        if(future1.isSuccess()){
                           channelPromise.trySuccess(future.channel());
                        }else{
                            channelPromise.tryFailure(future1.cause());
                        }
                    });
                    return;
                }
                log.info("connect {}:{} fail", remoteAddress, remotePort);
                channelPromise.tryFailure(future.cause());
            }
        });
        return channelPromise;
    }
}
