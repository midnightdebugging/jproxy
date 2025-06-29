package org.pierce.codec;

import com.google.gson.Gson;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.pierce.JproxyProperties;
import org.pierce.handler.DebugHandler;

import org.pierce.handler.TlsClientHandlerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SocksCommandClientTest {


    private static final Logger log = LoggerFactory.getLogger(SocksCommandClientTest.class);

    public static void main(String[] args) {

        int PORT = 20094;
        String ADDRESS = "127.0.0.1";
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group);
            b.channel(NioSocketChannel.class);
            b.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    if (JproxyProperties.booleanVal("tls-debug")) {
                        ch.pipeline().addLast(new DebugHandler("tls-link-out"));
                    }
                    if (true) {
                        ch.pipeline().addLast(TlsClientHandlerBuilder.getInstance().build(ch));
                    }
                    ch.pipeline().addLast(new DebugHandler());
                    ch.pipeline().addLast(new SocksCommandCodec());
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<SocksCommand>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, SocksCommand msg) throws Exception {
                            Gson gson = new Gson();
                            String json = gson.toJson(msg);
                            log.info(String.format("receive:%s=>%s", msg.getClass(), json));
                        }

                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            SocksCommandConnectRequest request1 = new SocksCommandConnectRequest();
                            request1.setTarget("sample.com001");
                            ctx.write(request1);
                            {
                                SocksCommandDNSRequest request2 = new SocksCommandDNSRequest();
                                request2.setDomain("sample.com003sdf");
                                ctx.write(request2);
                            }
                            {
                                SocksCommandDNSRequest request2 = new SocksCommandDNSRequest();
                                request2.setDomain("example.com");
                                ctx.write(request2);
                            }


                            SocksCommandClose request3 = new SocksCommandClose();
                            ctx.write(request3);
                            ctx.flush();
                            super.channelActive(ctx);
                        }
                    });
                }
            });
            log.info(String.format("connect:%s:%d", ADDRESS, PORT));
            ChannelFuture cf = b.connect(ADDRESS, PORT).sync();
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {

        }
    }
}
