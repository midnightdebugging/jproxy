package org.pierce.websocket.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.AttributeKey;
import org.pierce.MessageBridge;
import org.pierce.UtilTools;
import org.pierce.bridge.ByteBufMessageBridge;
import org.pierce.list.entity.TryConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import static io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker13.MAGIC_GUID;

public class RemoteWebSocketHandler extends ChannelDuplexHandler {


    private static final Logger log = LoggerFactory.getLogger(RemoteWebSocketHandler.class);

    //public static final AttributeKey<String> TARGET_ADDRESS = AttributeKey.valueOf("TARGET_ADDRESS");

    public static final AttributeKey<MessageBridge> MESSAGE_BRIDGE = AttributeKey.valueOf("MESSAGE_BRIDGE");

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if ((msg instanceof FullHttpRequest fullHttpRequest)) {
            if (!fullHttpRequest.decoderResult().isSuccess()) {
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(fullHttpRequest.protocolVersion(), HttpResponseStatus.BAD_REQUEST);
                ctx.writeAndFlush(response);
            }
            if (fullHttpRequest.headers().contains(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET, true)) {
                String workType = fullHttpRequest.headers().get("WORK_TYPE");
                String clientKey = fullHttpRequest.headers().get(HttpHeaderNames.SEC_WEBSOCKET_KEY);
                String serverKey = Base64.getEncoder().encodeToString(
                        MessageDigest.getInstance("SHA-1")
                                .digest((clientKey + MAGIC_GUID).getBytes(StandardCharsets.UTF_8))
                );
                if ("01".equals(workType)) {
                    String targetAddress = fullHttpRequest.headers().get("TARGET_ADDRESS");
                    int targetPort = Integer.parseInt(fullHttpRequest.headers().get("TARGET_PORT"));

                    ctx.channel().attr(ByteBufMessageBridge.TARGET_ADDRESS).set(targetAddress);
                    ctx.channel().attr(ByteBufMessageBridge.TARGET_PORT).set(targetPort);
                    MessageBridge messageBridge = new ByteBufMessageBridge();
                    ctx.channel().attr(MESSAGE_BRIDGE).set(messageBridge);
                    messageBridge.bridge(ctx.channel(), new TryConnect()).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {


                                DefaultFullHttpResponse response = new DefaultFullHttpResponse(fullHttpRequest.protocolVersion(), HttpResponseStatus.SWITCHING_PROTOCOLS);
                                response.headers().set(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET);
                                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE);
                                response.headers().set(HttpHeaderNames.SEC_WEBSOCKET_ACCEPT, serverKey);
                                ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                                    @Override
                                    public void operationComplete(ChannelFuture future) throws Exception {
                                        if (future.isSuccess()) {
                                            ctx.pipeline().remove("http-codec");
                                            ctx.pipeline().remove("http-aggregator");
                                            ctx.pipeline().addBefore(ctx.name(), "ws-decoder", new WebSocket13FrameDecoder(true, true, 65536, false));
                                            ctx.pipeline().addBefore(ctx.name(), "ws-encoder", new WebSocket13FrameEncoder(false));
                                        } else {
                                            DefaultFullHttpResponse response = new DefaultFullHttpResponse(fullHttpRequest.protocolVersion(), HttpResponseStatus.REQUEST_TIMEOUT);
                                            ctx.writeAndFlush(response);
                                        }
                                    }
                                });
                            } else {
                                DefaultFullHttpResponse response = new DefaultFullHttpResponse(fullHttpRequest.protocolVersion(), HttpResponseStatus.REQUEST_TIMEOUT);
                                ctx.writeAndFlush(response);
                            }
                        }
                    });
                } else {
                    DefaultFullHttpResponse response = new DefaultFullHttpResponse(fullHttpRequest.protocolVersion(), HttpResponseStatus.SWITCHING_PROTOCOLS);
                    response.headers().set(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET);
                    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE);
                    response.headers().set(HttpHeaderNames.SEC_WEBSOCKET_ACCEPT, serverKey);
                    ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                ctx.pipeline().remove("http-codec");
                                ctx.pipeline().remove("http-aggregator");
                                ctx.pipeline().addBefore(ctx.name(), "ws-decoder", new WebSocket13FrameDecoder(true, true, 65536, false));
                                ctx.pipeline().addBefore(ctx.name(), "ws-encoder", new WebSocket13FrameEncoder(false));
                            } else {
                                DefaultFullHttpResponse response = new DefaultFullHttpResponse(fullHttpRequest.protocolVersion(), HttpResponseStatus.REQUEST_TIMEOUT);
                                ctx.writeAndFlush(response);
                            }
                        }
                    });
                }

            }

            return;
        }
        if (msg instanceof BinaryWebSocketFrame binaryWebSocketFrame) {
            MessageBridge messageBridge = ctx.channel().attr(MESSAGE_BRIDGE).get();
            if (messageBridge != null) {
                try {
                    messageBridge.bridge(ctx.channel(), binaryWebSocketFrame.content().copy());
                } finally {
                    binaryWebSocketFrame.release();
                }

            } else {
                ctx.fireChannelRead(msg);
            }
        } else if (msg instanceof PongWebSocketFrame) {
            log.info("WebSocket Client received pong");
        } else if (msg instanceof CloseWebSocketFrame) {
            log.info("WebSocket Client received closing");
            ctx.close();
        }

    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        //log.info("==> {}:{}", UtilTools.formatChannelInfo(ctx), msg.getClass());
        if (msg instanceof ByteBuf) {

            BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame((ByteBuf) msg);
            ctx.write(binaryWebSocketFrame);
            return;
        }

        super.write(ctx, msg, promise);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.info("{} ", UtilTools.formatChannelInfo(ctx), cause);
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        log.info("{} ", UtilTools.formatChannelInfo(ctx));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("{} ", UtilTools.formatChannelInfo(ctx));
        Channel linkOut = ctx.channel().attr(MessageBridge.LINK_OUT).get();
        if (linkOut != null) {
            linkOut.close();
        }
    }
}
