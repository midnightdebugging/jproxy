package org.pierce.manage.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.pierce.UtilTools;
import org.pierce.manage.handler.impl.CliHttpMessageHandler;
import org.pierce.manage.handler.impl.DefaultHttpMessageHandler;
import org.pierce.manage.handler.impl.IndexHttpMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;

public class ManageServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger log = LoggerFactory.getLogger(ManageServerHandler.class);


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
        if (msg instanceof FullHttpRequest req) {

            boolean keepAlive = HttpUtil.isKeepAlive(req);
            HttpMessageHandler httpMessageHandler;

            if ("/".equals(req.uri()) || "/index.html".equals(req.uri())) {
                httpMessageHandler = new IndexHttpMessageHandler();
            } else if ("/cli".equals(req.uri())) {
                httpMessageHandler = new CliHttpMessageHandler();
            } else {
                httpMessageHandler = new DefaultHttpMessageHandler(404, "NOT_FOUND");
            }

            FullHttpResponse response = null;
            try {
                response = httpMessageHandler.handle(req);
            } catch (Throwable e) {
                StringWriter stringWriter = new StringWriter();
                e.printStackTrace(new PrintWriter(stringWriter));

                Map<String, String> map = new HashMap<>();
                map.put("message", stringWriter.toString());
                map.put("http-code", String.valueOf(500));

                response = handlerHttpError(req, UtilTools.objToString(map));
                log.info("Throwable e", e);
            }

            if (keepAlive) {
                if (!req.protocolVersion().isKeepAliveDefault()) {
                    response.headers().set(CONNECTION, KEEP_ALIVE);
                }
            } else {
                // Tell the client we're going to close the connection.
                response.headers().set(CONNECTION, CLOSE);
            }

            ChannelFuture f = ctx.write(response);

            if (!keepAlive) {
                f.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    public FullHttpResponse handlerHttpError(HttpRequest req, String message) {

        FullHttpResponse response = new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.INTERNAL_SERVER_ERROR,
                Unpooled.wrappedBuffer(message.getBytes()));
        response.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
                .setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.info("cause:", cause);
        ctx.close();
    }
}
