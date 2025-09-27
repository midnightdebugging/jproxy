package org.pierce.manage.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.compression.CompressionOptions;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import org.pierce.JproxyProperties;
import org.pierce.handler.DebugHandler;
import org.pierce.handler.TlsServerHandlerBuilder;

public class ManageServerInitializer extends ChannelInitializer<SocketChannel> {


    @Override
    public void initChannel(SocketChannel ch) {
        if (JproxyProperties.booleanVal("tls-debug")) {
            ch.pipeline().addLast(new DebugHandler("tls-link-in"));
        }
        // 添加 TLS 处理器到管道
        if (JproxyProperties.booleanVal("local-server.link-in.tls")) {
            ch.pipeline().addLast(TlsServerHandlerBuilder.getInstance().build(ch));
        }
        ch.pipeline().addLast("codec-anchor", new DebugHandler("link in"));

        ch.pipeline().addLast(new HttpServerCodec());
        ch.pipeline().addLast(new HttpObjectAggregator(8196));
        ch.pipeline().addLast(new HttpContentCompressor((CompressionOptions[]) null));
        ch.pipeline().addLast(new HttpServerExpectContinueHandler());
        ch.pipeline().addLast(new ManageServerHandler());
    }
}
