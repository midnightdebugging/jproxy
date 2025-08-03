package org.pierce.httpproxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.pierce.JproxyProperties;
import org.pierce.JproxyServer;
import org.pierce.httpproxy.handler.HttpProxyServerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpProxyServer implements JproxyServer {

    private static final Logger log = LoggerFactory.getLogger(HttpProxyServer.class);

    @Override
    public void start(EventLoopGroup eventLoopGroup) {
        int port = Integer.parseInt(JproxyProperties.getProperty("local-server.http-proxy.link-in.port"));

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(eventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new HttpProxyServerInitializer())
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        log.info("bind {}", port);
        ChannelFuture future0 = serverBootstrap.bind(port);
        future0.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future1) throws Exception {
                if (future1.isSuccess()) {
                    log.info("bind {} success", port);
                    future1.channel().closeFuture().addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future2) throws Exception {
                            log.info("bind {} close", port);
                        }
                    });
                    return;
                }
                log.info("bind {} fail", port);
            }
        });
    }

    public static void main(String[] args) {
        EventLoopGroup eventLoopGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                eventLoopGroup.shutdownGracefully();
                log.info("eventLoopGroup.shutdownGracefully()");
            }
        });

        JproxyServer httpProxyServer = new HttpProxyServer();
        httpProxyServer.start(eventLoopGroup);

    }


}