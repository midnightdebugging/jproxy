package org.pierce.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.pierce.handler.DebugHandler;

public class FixedChannelPoolClient {
    private final String host;
    private final int port;
    private FixedChannelPool channelPool;

    public FixedChannelPoolClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void init() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        //p.addLast(new StringDecoder());
                        // p.addLast(new StringEncoder());
                        p.addLast(new DebugHandler("link-out"));
                        /*p.addLast(new SimpleChannelInboundHandler<String>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                                System.out.println("Client received: " + msg);
                            }
                        });*/
                    }
                }).remoteAddress(host, port);

        // 创建FixedChannelPool
        channelPool = new FixedChannelPool(
                bootstrap,
                new FixedChannelPoolHandler(),
                5
        );
    }

    public void sendMessage(String message) {
        // 从连接池获取Channel
        Future<Channel> future = channelPool.acquire();

        future.addListener(new GenericFutureListener<Future<? super Channel>>() {
            @Override
            public void operationComplete(Future<? super Channel> future) throws Exception {
                if (future.isSuccess()) {
                    Channel channel = (Channel) future.get();


                    channel.writeAndFlush(Unpooled.wrappedBuffer(message.getBytes())).addListener((ChannelFutureListener) f -> {
                        // 无论发送成功与否，都要释放连接回连接池
                        channelPool.release(channel);
                        if (!f.isSuccess()) {
                            System.err.println("Send failed: " + f.cause().getMessage());
                        }
                    });
                }
            }
        });
        /*future.((channel, throwable) -> {
            if (throwable != null) {
                System.err.println("Failed to acquire channel: " + throwable.getMessage());
                return;
            }

            try {
                // 发送消息
                channel.writeAndFlush(message).addListener((ChannelFutureListener) f -> {
                    // 无论发送成功与否，都要释放连接回连接池
                    channelPool.release(channel);
                    if (!f.isSuccess()) {
                        System.err.println("Send failed: " + f.cause().getMessage());
                    }
                });
            } catch (Exception e) {
                channelPool.release(channel);
                System.err.println("Exception while sending: " + e.getMessage());
            }
        });*/
    }

    public void shutdown() {
        if (channelPool != null) {
            channelPool.close();
        }
    }

    // 连接池处理器
    private static class FixedChannelPoolHandler extends AbstractChannelPoolHandler {
        @Override
        public void channelCreated(Channel channel) {
            System.out.println("Channel created: " + channel.id());

            channel.pipeline().addLast(new DebugHandler("link-out"));

        }

        @Override
        public void channelAcquired(Channel ch) {
            System.out.println("Channel acquired: " + ch.id());
        }

        @Override
        public void channelReleased(Channel ch) {
            System.out.println("Channel released: " + ch.id());
        }
    }
}
