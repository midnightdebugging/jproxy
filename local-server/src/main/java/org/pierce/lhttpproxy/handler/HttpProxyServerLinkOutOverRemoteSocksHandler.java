package org.pierce.lhttpproxy.handler;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.concurrent.Promise;
import org.pierce.handler.LinkOutOverRemoteHandler;
import org.pierce.handler.LinkOutStatusEvent;
import org.pierce.handler.LinkOutStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class HttpProxyServerLinkOutOverRemoteSocksHandler extends LinkOutOverRemoteHandler {

    final static Logger log = LoggerFactory.getLogger(HttpProxyServerLinkOutOverRemoteSocksHandler.class);

    public HttpProxyServerLinkOutOverRemoteSocksHandler(Channel linkInChannel, Promise<Channel> promise, String targetAddress, int targetPort) {
        super(linkInChannel, promise, targetAddress, targetPort);
    }

    @Override
    public void linkInRead(Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;

            log.info(request.uri());
            URI uri = null;
            try {
                uri = new URI(request.uri());


                // 设置代理请求头
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 80 : uri.getPort();
                HttpRequest newRequest = new DefaultFullHttpRequest(
                        request.protocolVersion(),
                        request.method(),
                        uri.getRawPath() + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery())
                );
                newRequest.headers().setAll(request.headers());
                newRequest.headers().set(HttpHeaderNames.HOST, host);


                super.linkInRead(newRequest);
                return;
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        super.linkInRead(msg);
    }

    @Override
    public void linkOutStatusEvent(Channel linkIn, Channel linkOut, LinkOutStatusEvent linkOutStatusEvent) {

        if (linkOutStatusEvent.getLinkOutStep() == LinkOutStep.CONNECT_REMOTE_FINISH) {
            linkOut.pipeline().addBefore("LinkOutOverRemoteHandler-biz", HttpClientCodec.class.getName(), new HttpClientCodec());
        }
        super.linkOutStatusEvent(linkIn, linkOut, linkOutStatusEvent);

    }
}