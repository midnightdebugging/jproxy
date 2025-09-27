package org.pierce.manage.handler.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.pierce.manage.handler.HttpMessageHandler;

import java.io.IOException;
import java.io.InputStream;

public class IndexHttpMessageHandler implements HttpMessageHandler {
    @Override
    public FullHttpResponse handle(FullHttpRequest request) throws IOException {
        ByteBuf byteBuf = Unpooled.buffer();

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


            FullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.OK,
                    byteBuf);
            response.headers()
                    .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_HTML)
                    .setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            return response;
        } catch (Exception e) {
            byteBuf.release();
            throw e;
        }
    }
}
