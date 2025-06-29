package org.pierce.codec;

import com.google.gson.Gson;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.pierce.handler.DebugHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class SocksCommandServerTest {


    private static final Logger log = LoggerFactory.getLogger(SocksCommandServerTest.class);

    public static void main(String[] args) {

        int PORT = 3031;
        //final SslContext sslCtx = TLSUtil.createServerSslContext();
        //

        //
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .channel(NioServerSocketChannel.class)
                    //.handler(new DebugHandler())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new DebugHandler());
                            ch.pipeline().addLast(new SocksCommandCodec());
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<SocksCommand>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, SocksCommand msg) throws Exception {
                                    Gson gson = new Gson();
                                    String json = gson.toJson(msg);
                                    log.info("receive:{}=>{}", msg.getClass(), json);
                                    if (msg instanceof SocksCommandConnectRequest) {
                                        SocksCommandConnectResponse resp = new SocksCommandConnectResponse();
                                        resp.setCode(SocksCommandResponseCode.SUCCESS);
                                        //resp.setMessage("192.168.31.129");
                                        ctx.write(resp);
                                        ctx.flush();
                                        return;
                                    }
                                    if (msg instanceof SocksCommandDNSRequest) {
                                        SocksCommandDNSResponse resp = new SocksCommandDNSResponse();
                                        resp.setCode(SocksCommandResponseCode.SUCCESS);
                                        List<String> ipList = new ArrayList<>();
                                        ipList.add("127.0.0.1");
                                        ipList.add("127.0.0.2");
                                        resp.setIpList(ipList);
                                        ctx.write(resp);
                                        ctx.flush();
                                        return;
                                    }
                                    if (msg instanceof SocksCommandClose) {
                                        log.info("SocksCommandClose");
                                        ctx.channel().close();
                                        //ctx.close();

                                        return;
                                    }
                                    //throw new RuntimeException("非法的类型:" + msg.getClass());
                                    ByteBuf byteBuf = Unpooled.wrappedBuffer(json.getBytes());
                                    ctx.write(byteBuf);

                                }
                            });
                        }
                    });
            log.info(String.format("listen:%d", PORT));
            b.bind(PORT).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
