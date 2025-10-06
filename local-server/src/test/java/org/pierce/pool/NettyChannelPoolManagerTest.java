package org.pierce.pool;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.pierce.UtilTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class NettyChannelPoolManagerTest {

    private static final Logger log = LoggerFactory.getLogger(NettyChannelPoolManagerTest.class);

    public static void main(String[] args) {

        NettyChannelPoolManager httpPools = new NettyChannelPoolManager(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline().addLast(new HttpClientCodec());
                channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        log.info("{}:{}", UtilTools.formatChannelInfo(ctx), msg.getClass());
                        if (msg instanceof LastHttpContent) {
                            //需要完全响应完之后才能释放
                            NettyChannelPoolManager.release(channel);
                        }
                    }
                });
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    httpPools.close();
                    log.info("httpPools.close();");
                } catch (Exception e) {
                    log.info("Exception", e);
                }


            }
        });

        for (int i = 0; i < 10; i++) {
            // 异步获取一个连接
            Future<Channel> acquireFuture = httpPools.acquire("192.168.31.129", 80);

            // 添加监听器，在获取连接成功后进行操作
            acquireFuture.addListener((GenericFutureListener<Future<Channel>>) future -> {
                if (future.isSuccess()) {
                    Channel channel = future.getNow();
                    // 使用获取到的 Channel 发送请求
                    System.out.println("Using channel: " + channel.id() + " to send request");
                    DefaultHttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/frxxz/");
                    request.headers().set("Host", "192.168.31.129");
                    request.headers().set("User-Agent", "curl/8.11.0");
                    request.headers().set("Accept", "*/*");

                    channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                log.info("{}:发送成功",UtilTools.formatChannelInfo(channel), future.cause());
                                return;
                            }
                            log.info("{}:发送失败",UtilTools.formatChannelInfo(channel), future.cause());
                            StringBuilder sb = new StringBuilder();
                            ChannelPipeline pipeline = channel.pipeline();
                            sb.append("===== Pipeline Structure =====\n");
                            for (Map.Entry<String, ChannelHandler> entry : pipeline) {
                                String name = entry.getKey();
                                ChannelHandler handler = entry.getValue();
                                sb.append(String.format("Handler [%s] -> %s\n", name, handler.getClass().getSimpleName()));
                            }
                            log.info(sb.toString());
                        }
                    });
                    DefaultLastHttpContent defaultLastHttpContent=new DefaultLastHttpContent();
                    channel.writeAndFlush(defaultLastHttpContent);
                } else {
                    // 处理获取连接失败 (超时、连接池关闭等)
                    System.out.println("Failed to acquire channel: " + future.cause());
                }
            });
        }
    }
}
