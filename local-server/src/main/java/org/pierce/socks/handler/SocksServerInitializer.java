package org.pierce.socks.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.SocksPortUnificationServerHandler;
import org.pierce.JproxyProperties;
import org.pierce.handler.DebugHandler;
import org.pierce.handler.TlsServerHandlerBuilder;
import org.pierce.nlist.NameListCheck;
import org.pierce.session.SessionAttributes;


public final class SocksServerInitializer extends ChannelInitializer<SocketChannel> {


    NameListCheck nameListCheck;


    public SocksServerInitializer(NameListCheck nameListCheck) {
        this.nameListCheck = nameListCheck;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        if(nameListCheck!=null){
            ch.attr(SessionAttributes.NAME_LIST_CHECK).set(nameListCheck);
        }
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
