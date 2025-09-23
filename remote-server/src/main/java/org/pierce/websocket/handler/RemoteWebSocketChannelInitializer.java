package org.pierce.websocket.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.pierce.JproxyProperties;
import org.pierce.handler.DebugHandler;
import org.pierce.handler.TlsServerHandlerBuilder;


public class RemoteWebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    private static final String WEBSOCKET_PATH = "/websocket";

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        if (JproxyProperties.booleanVal("tls-debug")) {
            ch.pipeline().addLast(new DebugHandler("tls-link-in"));
        }
        if (JproxyProperties.booleanVal("remote-server.link-tls")) {
            ch.pipeline().addLast(TlsServerHandlerBuilder.getInstance().build(ch));
        }
        ch.pipeline().addLast(new DebugHandler("link-in"));

        ch.pipeline().addLast(new HttpServerCodec());
        ch.pipeline().addLast(new HttpObjectAggregator(65536));
        ch.pipeline().addLast(new RemoteWebSocketHttpHandler());
        ch.pipeline().addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));
        //ch.pipeline().addLast(new WebSocketFrameHandler());
    }

}
