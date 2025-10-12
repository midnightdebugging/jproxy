package org.pierce.manage.handler;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

public interface HttpMessageHandler {

    FullHttpResponse handle(FullHttpRequest request)  throws Throwable;

}
