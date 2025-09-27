package org.pierce.manage.handler.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.pierce.UtilTools;
import org.pierce.manage.handler.HttpMessageHandler;
import org.pierce.manage.handler.entity.DefaultReturn;

import java.io.IOException;

public class DefaultHttpMessageHandler implements HttpMessageHandler {

    DefaultReturn defaultReturn;

    public DefaultHttpMessageHandler(DefaultReturn defaultReturn) {
        this.defaultReturn = defaultReturn;
    }

    public DefaultHttpMessageHandler(int httpCode, String message) {
        this.defaultReturn = new DefaultReturn(httpCode, message);
    }

    @Override
    public FullHttpResponse handle(FullHttpRequest request) throws IOException {
        ByteBuf byteBuf = Unpooled.buffer();

        try {

            byteBuf.writeBytes(UtilTools.objToString(defaultReturn).getBytes());


            FullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.valueOf(defaultReturn.getHttpCode()),
                    byteBuf);
            response.headers()
                    .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                    .setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            return response;
        } catch (Exception e) {
            byteBuf.release();
            throw e;
        }
    }



}
