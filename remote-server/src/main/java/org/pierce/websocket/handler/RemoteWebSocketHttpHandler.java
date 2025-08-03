package org.pierce.websocket.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.pierce.codec.SocksCommandDuplexHandler;
import org.pierce.session.SessionAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

public class RemoteWebSocketHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


    private static final Logger log = LoggerFactory.getLogger(RemoteWebSocketHttpHandler.class);

/*    private final String websocketPath;

    public RemoteWebSocketHttpHandler(String websocketPath) {
        this.websocketPath = websocketPath;
    }*/

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        // Handle a bad request.
        if (!req.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(req.protocolVersion(), BAD_REQUEST,
                    ctx.alloc().buffer(0)));
            return;
        }

        // Handle websocket upgrade request.
        if (req.headers().contains(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET, true)) {
            String workType = req.headers().get("WORK_TYPE");
            if ("01".equals(workType)) {
                ctx.channel().attr(SessionAttributes.TARGET_ADDRESS).set(req.headers().get("TARGET_ADDRESS"));
                ctx.channel().attr(SessionAttributes.TARGET_PORT).set(Integer.parseInt(req.headers().get("TARGET_PORT")));
                ctx.channel().attr(SessionAttributes.WORK_TYPE).set(req.headers().get("WORK_TYPE"));
                ctx.channel().pipeline().addLast(new WebSocketFrameHandler());
                ctx.fireChannelRead(req.retain());
            } else {
                ctx.channel().attr(SessionAttributes.WORK_TYPE).set(req.headers().get("WORK_TYPE"));
                ctx.channel().pipeline().addLast(new SocksCommandDuplexHandler());
                ctx.channel().pipeline().addLast(new RemoteSocksHandler());
                ctx.fireChannelRead(req.retain());
            }

            return;
        }

        // Allow only GET methods.
        if (!GET.equals(req.method())) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(req.protocolVersion(), FORBIDDEN,
                    ctx.alloc().buffer(0)));
            return;
        }
        ByteBuf byteBuf = Unpooled.buffer();
        // Send the index page
        if ("/".equals(req.uri()) || "/index.html".equals(req.uri())) {
            try (InputStream is = getClass().getResourceAsStream("/web/index.html")) {
                byte[] bytes = new byte[1024];
                // 使用字节数组作为中间缓冲区
                byte[] temp = new byte[8192]; // 建议bufferSize为8KB（8192）
                int bytesRead;
                if (is != null) {
                    while ((bytesRead = is.read(temp)) != -1) {
                        // 将数据写入ByteBuf（避免复制，直接通过数组写入）
                        byteBuf.writeBytes(temp, 0, bytesRead);
                    }
                }

            }
            FullHttpResponse res = new DefaultFullHttpResponse(req.protocolVersion(), OK, byteBuf);

            res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
            HttpUtil.setContentLength(res, byteBuf.readableBytes());

            sendHttpResponse(ctx, req, res);
        } else {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(req.protocolVersion(), NOT_FOUND,
                    ctx.alloc().buffer(0)));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("cause", cause);
        ctx.close();
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        HttpResponseStatus responseStatus = res.status();
        if (responseStatus.code() != 200) {
            ByteBufUtil.writeUtf8(res.content(), responseStatus.toString());
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }
        // Send the response and close the connection if necessary.
        boolean keepAlive = HttpUtil.isKeepAlive(req) && responseStatus.code() == 200;
        HttpUtil.setKeepAlive(res, keepAlive);
        ChannelFuture future = ctx.writeAndFlush(res);
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

}
