package org.pierce.socks;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.pierce.JproxyProperties;
import org.pierce.handler.DebugHandler;

import java.util.logging.Logger;

public class EchoServer {

    private static final Logger log = Logger.getLogger(String.valueOf(EchoServer.class));

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        int port = Integer.parseInt(JproxyProperties.getProperty("echo.link-in.port", "20094"));
        log.info(String.format("listening port:%d", port));
        try {
            new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new DebugHandler("echoServer-link-in"));
                            ch.pipeline().addLast(new EchoServerHandler());
                        }
                    })//.childOption(ChannelOption.AUTO_READ, true)
                    .bind(port).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static class EchoServerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ctx.writeAndFlush(msg);

        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.read();
        }
    }

}
