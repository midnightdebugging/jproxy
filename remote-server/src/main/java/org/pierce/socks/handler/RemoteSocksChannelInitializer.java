package org.pierce.socks.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.pierce.JproxyProperties;
import org.pierce.codec.SocksCommandCodec;
import org.pierce.handler.DebugHandler;
import org.pierce.handler.TlsServerHandlerBuilder;


public class RemoteSocksChannelInitializer extends ChannelInitializer<SocketChannel> {



    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        if (JproxyProperties.booleanVal("tls-debug")) {
            ch.pipeline().addLast(new DebugHandler("tls-link-in"));
        }
        if (JproxyProperties.booleanVal("remote-socks.link-tls")) {
            ch.pipeline().addLast(TlsServerHandlerBuilder.getInstance().build(ch));
        }
        ch.pipeline().addLast(new DebugHandler("link-in"));
        ch.pipeline().addLast(new SocksCommandCodec());
        ch.pipeline().addLast(new RemoteSocksHandler());
    }
}
