package org.pierce.lhttpproxy.handler;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.*;
import org.pierce.LocalServer;
import org.pierce.UtilTools;
import org.pierce.entity.ConnectType;
import org.pierce.entity.LocalLinkStatusEvent;
import org.pierce.entity.LocalLinkStep;
import org.pierce.entity.ProtocolInfo;
import org.pierce.handler.LinkOutHandler;
import org.pierce.handler.LinkOutOverRemoteHandler;
import org.pierce.nlist.Directive;
import org.pierce.session.SessionAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;

import static io.netty.handler.codec.http.HttpHeaderNames.*;

public class HttpProxyServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static final Logger log = LoggerFactory.getLogger(HttpProxyServerHandler.class);

    Queue<Object> queue = new LinkedList<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        log.info("{} {} ", UtilTools.formatChannelInfo(ctx), msg.getClass());
        if (msg instanceof HttpRequest) {

            connect(ctx, msg);
        }
        queue.add(msg);

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
    }

    public void connect(ChannelHandlerContext ctx, HttpObject msg) {

        HttpRequest request = (HttpRequest) msg;
        String uri = request.uri();
        log.info(uri);
        ProtocolInfo protocolInfo = UtilTools.parseProtocolInfo(uri);


        String targetAddress = protocolInfo.getHostAddress();
        int targetPort = protocolInfo.getPort();

        ctx.channel().attr(SessionAttributes.TARGET_ADDRESS).set(targetAddress);
        ctx.channel().attr(SessionAttributes.TARGET_PORT).set(targetPort);
        ctx.channel().attr(SessionAttributes.REQUEST_METHOD).set(request.method().name());

        EventExecutor executor = ImmediateEventExecutor.INSTANCE;

        Promise<ConnectType> promise = new DefaultPromise<>(executor);

        promise.addListener(future -> {
            if (future.isSuccess()) {
                ConnectType connectType = (ConnectType) future.getNow();
                log.info("{} {} ==> {}", UtilTools.formatChannelInfo(ctx), targetAddress, connectType.getAddress());
                if (connectType.getDirective() == Directive.DISALLOW_CONNECT) {
                    FullHttpResponse response = new DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1,
                            HttpResponseStatus.FORBIDDEN
                    );
                    response.headers()
                            .set(CONTENT_TYPE, "text/plain; charset=UTF-8")
                            .set(CONTENT_LENGTH, response.content().readableBytes())
                            .set(CONNECTION, HttpHeaderValues.CLOSE);

                    ctx.writeAndFlush(response).addListener(future2 -> {
                        if (ctx.channel().isActive()) {
                            ctx.channel().close();
                        }
                    });
                } else if (connectType.getDirective() == Directive.DIRECT_CONNECT) {
                    directConnect(ctx, connectType.getAddress(), targetPort);
                } else if (connectType.getDirective() == Directive.FULL_CONNECT) {
                    fullConnect(ctx, connectType.getAddress(), targetPort);
                }
            } else {
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.BAD_GATEWAY
                );

                // 3. 设置响应头
                response.headers()
                        .set(CONTENT_TYPE, "text/plain; charset=UTF-8")
                        .set(CONTENT_LENGTH, response.content().readableBytes())
                        .set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);

                ctx.writeAndFlush(response).addListener(future1 -> {
                    if (ctx.channel().isActive()) {
                        ctx.channel().close();
                    }
                });

            }

        });


        LocalServer.getInstance().connectionTypeCheck.check(ctx.channel().eventLoop(), targetAddress, targetPort, promise);
    }


    public void directConnect(ChannelHandlerContext ctx, String address, int port) {


        Promise<Channel> promise = ImmediateEventExecutor.INSTANCE.newPromise();
        LinkOutHandler handler = null;
        if ("CONNECT".equals(ctx.channel().attr(SessionAttributes.REQUEST_METHOD).get())) {
            handler = new LinkOutHandler(ctx.channel(), promise, address, port);
        } else {
            handler = new HttpProxyServerLinkOutDirectHandler(ctx.channel(), promise, address, port);
        }


        LinkOutHandler finalHandler = handler;
        promise.addListener(future -> {

            if (!ctx.channel().isActive()) {
                ctx.channel().close();
                return;
            }
            ctx.pipeline().addLast(finalHandler);

            if (!ctx.isRemoved()) {
                ctx.pipeline().remove(HttpProxyServerHandler.class);

            }
            while (!queue.isEmpty()) {
                ctx.fireChannelRead(queue.remove());
            }
            if (future.isSuccess()) {
                localLinkStatusEvent(ctx.channel(), new LocalLinkStatusEvent(LocalLinkStep.CONNECT_FINISH, true, address, port));
                return;
            }
            localLinkStatusEvent(ctx.channel(), new LocalLinkStatusEvent(LocalLinkStep.CONNECT_FINISH, false, future.cause()));

        });
    }

    public void fullConnect(ChannelHandlerContext ctx, String address, int port) {
        Promise<Channel> promise = ImmediateEventExecutor.INSTANCE.newPromise();
        LinkOutHandler handler = null;

        if ("CONNECT".equals(ctx.channel().attr(SessionAttributes.REQUEST_METHOD).get())) {
            handler = new LinkOutOverRemoteHandler(ctx.channel(), promise, address, port);
        } else {
            handler = new HttpProxyServerLinkOutOverRemoteSocksHandler(ctx.channel(), promise, address, port);
        }

        LinkOutHandler finalHandler = handler;

        promise.addListener(future -> {

            if (!ctx.channel().isActive()) {
                ctx.channel().close();
                return;
            }
            ctx.pipeline().addLast(finalHandler);

            if (!ctx.isRemoved()) {
                ctx.pipeline().remove(HttpProxyServerHandler.class);
            }
            while (!queue.isEmpty()) {
                ctx.fireChannelRead(queue.remove());
            }
            if (future.isSuccess()) {
                localLinkStatusEvent(ctx.channel(), new LocalLinkStatusEvent(LocalLinkStep.CONNECT_FINISH, true, address, port));
                return;
            }
            localLinkStatusEvent(ctx.channel(), new LocalLinkStatusEvent(LocalLinkStep.CONNECT_FINISH, false, future.cause()));

        });
    }

    public void localLinkStatusEvent(Channel channel, LocalLinkStatusEvent linkOutStatusEvent) {
        log.info("{} {} ", UtilTools.formatChannelInfo(channel), UtilTools.objToString(linkOutStatusEvent));
        if ("CONNECT".equals(channel.attr(SessionAttributes.REQUEST_METHOD).get())) {
            if (!linkOutStatusEvent.isSuccess()) {
                log.info("localLinkStatusEvent,linkOutStatusEvent.getCause()", linkOutStatusEvent.getCause());

                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.REQUEST_TIMEOUT
                );
                response.headers()
                        .set(CONTENT_TYPE, "text/plain; charset=UTF-8")
                        .set(CONTENT_LENGTH, response.content().readableBytes())
                        .set(CONNECTION, HttpHeaderValues.CLOSE);
                channel.writeAndFlush(response).addListener(future -> {
                    if (channel.isActive()) {
                        channel.close();
                    }
                });
                return;
            }

            if (linkOutStatusEvent.getLinkOutStep() == LocalLinkStep.CONNECT_FINISH) {
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK
                );
                response.headers()
                        .set(CONTENT_TYPE, "text/plain; charset=UTF-8")
                        .set(CONTENT_LENGTH, response.content().readableBytes())
                        .set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                channel.writeAndFlush(response).addListener((ChannelFutureListener) future -> channel.pipeline().remove(HttpServerCodec.class));
            }
        }

    }

}