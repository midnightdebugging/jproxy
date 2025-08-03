package org.pierce.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.util.concurrent.Promise;
import org.pierce.JproxyProperties;
import org.pierce.UtilTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;


public class LinkOutOverWebSocketHandler extends LinkOutHandler {


    final static Logger log = LoggerFactory.getLogger(LinkOutOverWebSocketHandler.class);


    final static String address = JproxyProperties.getProperty("local-server.link-out.address");

    final static int port = Integer.parseInt(JproxyProperties.getProperty("local-server.remote-websocket-link-out.port"));

    public LinkOutOverWebSocketHandler(Channel linkInChannel, Promise<Channel> promise, String targetAddress, int targetPort) {
        super(linkInChannel, promise, address, port, targetAddress, targetPort);
    }

    @Override
    protected void initChannel(SocketChannel ch, Promise<Channel> promise) throws URISyntaxException {

        String url = JproxyProperties.evaluate("${local-server.link-out.address}:${local-server.remote-websocket-link-out.port}/${local-server.link-out.websocket-path}");
        if (JproxyProperties.booleanVal("local-server.link-out.tls")) {
            url = "wss://" + url;
        } else {
            url = "ws://" + url;
        }
        URI uri = new URI(url);
        final WebSocketClientHandler handler =
                new WebSocketClientHandler(this,
                        WebSocketClientHandshakerFactory.newHandshaker(
                                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders() {
                                    {
                                        add("TARGET_ADDRESS", LinkOutOverWebSocketHandler.this.targetAddress);
                                        add("TARGET_PORT", LinkOutOverWebSocketHandler.this.targetPort);
                                        add("WORK_TYPE", "01");
                                    }
                                }));

        if (JproxyProperties.booleanVal("tls-debug")) {
            ch.pipeline().addLast(new DebugHandler("tls-link-out"));
        }
        if (JproxyProperties.booleanVal("local-server.link-out.tls")) {
            ch.pipeline().addLast(TlsClientHandlerBuilder.getInstance().build(ch));
        }
        ch.pipeline().addLast(new DebugHandler("link-out-via-websocket"));
        ch.pipeline().addLast(new HttpClientCodec());
        ch.pipeline().addLast(new HttpObjectAggregator(8192));
        ch.pipeline().addLast(handler);

    }

/*    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame frame = ((BinaryWebSocketFrame) msg);
            //Unpooled.buffer().writeBytes(request.content().copy());
            //ctx.fireChannelRead(frame.content().copy());
            if (linkOutChannel != null) {
                if (!linkOutChannel.isActive()) {
                    linkInChannel.close();
                    return;
                }
                linkOutChannel.writeAndFlush(frame);
            }
        }
    }*/

    @Override
    public void linkInRead(Object msg) {
        log.debug("{} {}", UtilTools.formatChannelInfo(linkInChannel), msg.getClass());
        //ByteBuf >> BinaryWebSocketFrame
        if (msg instanceof ByteBuf) {
            BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame((ByteBuf) msg);
            linkOutChannel.writeAndFlush(binaryWebSocketFrame);
        }
    }

    @Override
    public void linkOutRead(Object msg) {
        log.debug("{} {}", UtilTools.formatChannelInfo(linkOutChannel), msg.getClass());
        //BinaryWebSocketFrame >> ByteBuf
        if (msg instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame frame = ((BinaryWebSocketFrame) msg);
            linkInChannel.writeAndFlush(frame.content().copy());
        }
    }

    @Override
    public void linkOutStatusEvent(Channel linkIn, Channel linkOut, LinkOutStatusEvent linkOutStatusEvent) {
        log.info("{} {} ", UtilTools.formatChannelInfo(linkIn), UtilTools.objToString(linkOutStatusEvent));
        if (!linkOutStatusEvent.isSuccess()) {
            this.promise.tryFailure(linkOutStatusEvent.getCause());
            return;
        }
        log.info("{} {}||{}||{} ", UtilTools.formatChannelInfo(linkIn), linkOutStatusEvent.getLinkOutStep(), LinkOutStep.CONNECT_REMOTE_FINISH, linkOutStatusEvent.getLinkOutStep() == LinkOutStep.CONNECT_REMOTE_FINISH);
        if (linkOutStatusEvent.getLinkOutStep() == LinkOutStep.CONNECT_REMOTE_FINISH) {

            while (!queue.isEmpty()) {
                Object msg = queue.remove();
                log.info(String.valueOf(msg));
                linkInRead(msg);
            }
            this.promise.setSuccess(linkOut);
        }
    }
}
