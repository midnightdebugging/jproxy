package org.pierce.socks;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.pierce.UtilTools;
import org.pierce.codec.SocksCommandCodec;
import org.pierce.codec.SocksCommandConnectRequest;
import org.pierce.codec.SocksCommandConnectResponse;
import org.pierce.codec.SocksCommandResponseCode;
import org.pierce.handler.DebugHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RServerClientTest {


    private static final Logger log = LoggerFactory.getLogger(RServerClientTest.class);

    public static void main(String[] args) {
        int PORT = 3031;
        String ADDRESS = "127.0.0.1";
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group);
            b.channel(NioSocketChannel.class);
            b.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new DebugHandler("link-out"));
                    ch.pipeline().addLast(new SocksCommandCodec());
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        ChannelInboundHandlerAdapter _this = this;

                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.info(String.format("receive:%s=>%s", msg.getClass(), UtilTools.objToString(msg)));
                            if (msg instanceof SocksCommandConnectResponse) {
                                SocksCommandResponseCode code = ((SocksCommandConnectResponse) msg).getCode();
                                if (code == SocksCommandResponseCode.SUCCESS) {
                                    ByteBuf byteBuf = Unpooled.buffer(1024);
                                    byteBuf.writeBytes("GET / HTTP/1.1\r\nHost: 192.168.31.129\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n\r\n".getBytes());

                                    ChannelFuture cf = ctx.writeAndFlush(byteBuf);
                                    ctx.pipeline().remove(_this);
                                    ctx.pipeline().remove(SocksCommandCodec.class);

                                    ChannelPipeline pipeline = ctx.pipeline();

                                    cf.addListener(new GenericFutureListener<Future<? super Void>>() {
                                        @Override
                                        public void operationComplete(Future<? super Void> future) throws Exception {
                                           // ctx.pipeline().remove(_this);
                                            //ctx.pipeline().remove(SocksCommandCodec.class);
                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            SocksCommandConnectRequest request1 = new SocksCommandConnectRequest();
                            request1.setTarget("192.168.31.129");
                            request1.setPort(80);
                            ctx.write(request1);
                            ctx.flush();
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
