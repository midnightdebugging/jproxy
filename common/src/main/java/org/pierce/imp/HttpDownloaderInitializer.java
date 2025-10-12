package org.pierce.imp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.concurrent.Promise;
import org.pierce.JproxyProperties;
import org.pierce.UtilTools;
import org.pierce.entity.ProtocolInfo;
import org.pierce.handler.DebugHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class HttpDownloaderInitializer extends ChannelInitializer<SocketChannel> {

    private final static Logger log = LoggerFactory.getLogger(HttpDownloaderInitializer.class);

    Promise<Channel> promise;

    String url;

    String savePath;

    ProtocolInfo protocolInfo;

    String proxyAddress = JproxyProperties.getProperty("local-server.manage.proxy.link-out.address");

    int proxyPort = Integer.parseInt(JproxyProperties.getProperty("local-server.manage.proxy.link-out.port"));

    public HttpDownloaderInitializer(String savePath, String url, Promise<Channel> promise) {
        this.savePath = savePath;
        this.url = url;
        this.promise = promise;
        this.protocolInfo = UtilTools.parseProtocolInfo(url);
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        // 配置代理服务器地址
        log.info("proxyAddress:{}", proxyAddress);
        log.info("proxyPort:{}", proxyPort);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(proxyAddress, proxyPort);
        // 添加 HTTP 代理处理器
        ch.pipeline().addLast(new HttpProxyHandler(inetSocketAddress));

        if ("https://".equals(protocolInfo.getProtocol())) {
            SslContext sslCtx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()));
        } else if (!"http://".equals(protocolInfo.getProtocol())) {
            throw new RuntimeException(String.format("protocol [%s] must be https:// or http:// .", protocolInfo.getProtocol()));
        }
        if (JproxyProperties.booleanVal("debug")) {
            ch.pipeline().addLast(new DebugHandler());
        }
        ch.pipeline().addLast(new HttpClientCodec());

        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            boolean saveFile = false;

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                log.info("{}", UtilTools.formatChannelInfo(ctx), cause);
                promise.tryFailure(cause);
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                log.info("1-{}:{}", UtilTools.formatChannelInfo(ctx), msg.getClass());
                if (msg instanceof DefaultHttpResponse) {
                    log.debug("DefaultHttpResponse-{}:{}", UtilTools.formatChannelInfo(ctx), msg.getClass());
                    if (((DefaultHttpResponse) msg).status().code() != 200) {
                        ctx.channel().close();
                        promise.tryFailure(new RuntimeException(String.format("download:%s ==> status().code():%d", url, ((DefaultHttpResponse) msg).status().code())));
                    } else {
                        saveFile = true;
                    }
                    return;
                }
                if (msg instanceof HttpContent) {
                    if (!saveFile) {
                        return;
                    }
                    ByteBuf byteBuf = ((HttpContent) msg).content().copy();
                    byte[] bytes = ByteBufUtil.getBytes(byteBuf);
                    log.debug("HttpContent-{}:{}", UtilTools.formatChannelInfo(ctx), msg.getClass());
                    Files.write(Path.of(savePath), bytes, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    if (msg instanceof LastHttpContent) {
                        promise.trySuccess(ctx.channel());
                        ctx.close();
                    }
                }
            }

            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                FullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, protocolInfo.getPath());
                fullHttpRequest.headers().set("Host", protocolInfo.getHostAddress());
                fullHttpRequest.headers().set("User-Agent", "curl/8.14.1");
                fullHttpRequest.headers().set("Accept", "*/*");
                ctx.writeAndFlush(fullHttpRequest);
            }
        });
    }
}
