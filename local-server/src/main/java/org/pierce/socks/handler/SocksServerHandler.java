package org.pierce.socks.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.SocksMessage;
import io.netty.handler.codec.socksx.v5.*;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;
import org.pierce.LocalServer;
import org.pierce.UtilTools;
import org.pierce.entity.ConnectType;
import org.pierce.entity.LocalLinkStatusEvent;
import org.pierce.entity.LocalLinkStep;
import org.pierce.handler.LinkOutHandler;
import org.pierce.handler.LinkOutOverWebSocketHandler;
import org.pierce.nlist.Directive;
import org.pierce.session.SessionAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class SocksServerHandler extends SimpleChannelInboundHandler<SocksMessage> {

    //static NameListCheck check = LocalServer.getNameListCheck();

    private static final Logger log = LoggerFactory.getLogger(SocksServerHandler.class);

    public SocksServerHandler() {
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, SocksMessage socksRequest) throws Exception {
        log.info("{}", UtilTools.formatChannelInfo(ctx));
        switch (socksRequest.version()) {
            case SOCKS4a:
            case UNKNOWN:
                ctx.close();
                break;
            case SOCKS5:
                if (socksRequest instanceof Socks5InitialRequest) {
                    ctx.pipeline().addAfter("codec-anchor", "socks-codec", new Socks5CommandRequestDecoder());
                    ctx.write(new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH));
                } else if (socksRequest instanceof Socks5PasswordAuthRequest) {
                    ctx.pipeline().addAfter("codec-anchor", "socks-codec", new Socks5CommandRequestDecoder());
                    ctx.write(new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.SUCCESS));
                } else if (socksRequest instanceof Socks5CommandRequest) {
                    Socks5CommandRequest socks5CmdRequest = (Socks5CommandRequest) socksRequest;
                    //String mode = JproxyProperties.getProperty("socks.server.middle-mode");
                    if (socks5CmdRequest.type() == Socks5CommandType.CONNECT) {
                        String address = socks5CmdRequest.dstAddr();
                        int port = socks5CmdRequest.dstPort();
                        Channel channel = ctx.channel();

                        channel.attr(SessionAttributes.TARGET_ADDRESS).set(address);
                        channel.attr(SessionAttributes.TARGET_PORT).set(port);
                        //SocksServer.eventLog.info(ctx, "接入");
                        connect(ctx, socks5CmdRequest);
                        return;
                    } else {
                        ctx.close();
                    }
                } else {
                    ctx.close();
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + socksRequest.version());
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        log.info("{}", UtilTools.formatChannelInfo(ctx));
        ctx.flush();

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("{}", UtilTools.formatChannelInfo(ctx));
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {

        log.info("{}", UtilTools.formatChannelInfo(ctx), throwable);
        ctx.channel().close();
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        log.info("{}", UtilTools.formatChannelInfo(ctx));
    }


    private void connect(ChannelHandlerContext ctx, Socks5CommandRequest socks5CmdRequest) {

        String host = socks5CmdRequest.dstAddr();
        int port = socks5CmdRequest.dstPort();

        EventExecutor executor = ImmediateEventExecutor.INSTANCE;

        Promise<ConnectType> promise = new DefaultPromise<>(executor);

        promise.addListener(future -> {
            if (future.isSuccess()) {
                ConnectType connectType = (ConnectType) future.getNow();
                log.info("{} {} {} ==> {}", UtilTools.formatChannelInfo(ctx), connectType.getDirective(), host, connectType.getAddress());
                if (connectType.getDirective() == Directive.DISALLOW_CONNECT) {
                    //localLinkStatusEvent(ctx.channel(), new LocalLinkStatusEvent(LocalLinkStep.CONNECT_START, false, new RuntimeException("GFWDirective.DISALLOW_CONNECT")));
                    directConnect(ctx, connectType.getAddress(), port);
                } else if (connectType.getDirective() == Directive.DIRECT_CONNECT) {
                    directConnect(ctx, connectType.getAddress(), port);

                } else if (connectType.getDirective() == Directive.FULL_CONNECT) {
                    fullConnect(ctx, connectType.getAddress(), port);

                }
            } else {
                localLinkStatusEvent(ctx.channel(), new LocalLinkStatusEvent(LocalLinkStep.CONNECT_START, false, future.cause()));
            }
            return;

        });


        LocalServer.getInstance().connectionTypeCheck.check(ctx.channel(), host, port, promise);
    }

    public void directConnect(ChannelHandlerContext ctx, String address, int port) {


        Promise<Channel> promise = ImmediateEventExecutor.INSTANCE.newPromise();
        LinkOutHandler handler = new LinkOutHandler(ctx.channel(), promise, address, port);

        promise.addListener(future -> {

            if (!ctx.channel().isActive()) {
                ctx.channel().close();
                return;
            }
            ctx.pipeline().addLast(handler);

            if (!ctx.isRemoved()) {
                ctx.pipeline().remove(SocksServerHandler.class);
                //ctx.pipeline().remove(SocksPortUnificationServerHandler.class);
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
        LinkOutHandler handler = new LinkOutOverWebSocketHandler(ctx.channel(), promise, address, port);

        promise.addListener(future -> {

            if (!ctx.channel().isActive()) {
                ctx.channel().close();
                return;
            }
            ctx.pipeline().addLast(handler);

            if (!ctx.isRemoved()) {
                ctx.pipeline().remove(SocksServerHandler.class);
                //ctx.pipeline().remove(SocksPortUnificationServerHandler.class);
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
        if (!linkOutStatusEvent.isSuccess()) {
            log.info("localLinkStatusEvent,linkOutStatusEvent.getCause()", linkOutStatusEvent.getCause());

            DefaultSocks5CommandResponse resp = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
            channel.writeAndFlush(resp).addListener(future -> {
                if (channel.isActive()) {
                    channel.close();
                }
            });
            return;
        }

        if (linkOutStatusEvent.getLinkOutStep() == LocalLinkStep.CONNECT_FINISH) {
            DefaultSocks5CommandResponse resp = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
            channel.writeAndFlush(resp);
        }
    }
}
