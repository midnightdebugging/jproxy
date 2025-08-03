package org.pierce.socks;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.pierce.JproxyProperties;
import org.pierce.JproxyServer;
import org.pierce.LocalServer;
import org.pierce.socks.handler.SocksServerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class SocksServer implements JproxyServer {


    private static final Logger log = LoggerFactory.getLogger(SocksServer.class);

    @Override
    public void start(EventLoopGroup eventLoopGroup) {

        System.setProperty("javax.net.debug", "ssl:handshake");
        LocalServer.getInstance().initialize();

        int port = Integer.parseInt(JproxyProperties.getProperty("local-server.socks.link-in.port", "20084"));


        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(eventLoopGroup)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .channel(NioServerSocketChannel.class)
                .childHandler(new SocksServerInitializer());
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
        JproxyServer socksServer = new SocksServer();
        socksServer.start(eventLoopGroup);

    }


}
