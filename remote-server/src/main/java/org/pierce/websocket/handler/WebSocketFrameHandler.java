package org.pierce.websocket.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.concurrent.Promise;
import org.pierce.RemoteServer;
import org.pierce.UtilTools;
import org.pierce.handler.LinkOutHandler;
import org.pierce.nlist.Directive;
import org.pierce.session.SessionAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Echoes uppercase content of text frames.
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    boolean linkOutComplete = false;

    Queue<Object> queue = new LinkedList<>();

    private static final Logger log = LoggerFactory.getLogger(WebSocketFrameHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        log.info("{} {}", UtilTools.formatChannelInfo(ctx), frame);
        if (!linkOutComplete) {
            queue.add(frame.copy());
            return;
        }
        ctx.fireChannelRead(frame);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            //Channel upgrade to websocket, remove RemoteWebSocketHttpHandler.
            ctx.pipeline().remove(RemoteWebSocketHttpHandler.class);
            connect(ctx);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("========================");

    }

    public void connect(ChannelHandlerContext ctx) {
        String address = ctx.channel().attr(SessionAttributes.TARGET_ADDRESS).get();
        int port = ctx.channel().attr(SessionAttributes.TARGET_PORT).get();

        log.info("{}:{}", address, port);
        Directive directive = RemoteServer.getNameListCheck().check(address, port);
        if (directive == Directive.DISALLOW_CONNECT) {
            if (ctx.channel().isActive()) {
                ctx.channel().close();
            }
        }

        Promise<Channel> promise = ctx.executor().newPromise();
        promise.addListener(future -> {
            if (!future.isSuccess()) {
                if (ctx.channel().isActive()) {
                    ctx.channel().close();
                }
                return;
            }
            log.info("success!");
            linkOutComplete = true;
            while (!queue.isEmpty()) {
                ctx.fireChannelRead(queue.remove());
            }
            if (ctx.channel().isActive()) {
                if (!ctx.isRemoved()) {
                    ctx.channel().pipeline().remove(this);
                }
            }

        });
        ctx.channel().eventLoop().execute(() -> {
            try {
                InetAddress inetAddress = InetAddress.getByName(address);
                String newAddress = inetAddress.getHostAddress();
                Directive directive1 = RemoteServer.getNameListCheck().check(address, port);
                if (directive1 == Directive.DISALLOW_CONNECT) {
                    if (ctx.channel().isActive()) {
                        ctx.channel().close();
                    }
                }
                ctx.pipeline().addLast(new LinkOutHandler(ctx.channel(), promise, newAddress, port) {

                    //BinaryWebSocketFrame to bytes
                    @Override
                    public void linkInRead(Object msg) {
                        log.info("{} {}", UtilTools.formatChannelInfo(ctx), msg);
                        if (!(msg instanceof BinaryWebSocketFrame)) {
                            return;
                        }
                        if (this.getLinkOutChannel().isActive()) {
                            this.getLinkOutChannel().writeAndFlush(((BinaryWebSocketFrame) msg).content().copy());
                        }

                    }

                    //byte to BinaryWebSocketFrame
                    @Override
                    public void linkOutRead(Object msg) {
                        log.info("{} {}", UtilTools.formatChannelInfo(ctx), msg);
                        if (!(msg instanceof ByteBuf)) {
                            return;
                        }
                        if (this.getLinkInChannel().isActive()) {
                            this.getLinkInChannel().writeAndFlush(new BinaryWebSocketFrame((ByteBuf) msg));
                        }
                    }
                });
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
