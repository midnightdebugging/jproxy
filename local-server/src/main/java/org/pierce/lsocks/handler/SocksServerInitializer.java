package org.pierce.lsocks.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.SocksPortUnificationServerHandler;
import org.pierce.JproxyProperties;
import org.pierce.handler.DebugHandler;
import org.pierce.handler.TlsServerHandlerBuilder;


public final class SocksServerInitializer extends ChannelInitializer<SocketChannel> {


    @Override
    public void initChannel(SocketChannel ch) throws Exception {

        if (JproxyProperties.booleanVal("tls-debug")) {
            ch.pipeline().addLast(new DebugHandler("tls-link-in"));
        }
        // 添加 TLS 处理器到管道
        if (JproxyProperties.booleanVal("local-server.link-in.tls")) {
            ch.pipeline().addLast(TlsServerHandlerBuilder.getInstance().build(ch));
        }
        ch.pipeline().addLast("codec-anchor", new DebugHandler("link in"));

        ch.pipeline().addLast(new SocksPortUnificationServerHandler());
        ch.pipeline().addLast(new SocksServerHandler());
    }
}
