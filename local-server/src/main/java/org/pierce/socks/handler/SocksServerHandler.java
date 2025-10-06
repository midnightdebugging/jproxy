package org.pierce.socks.handler;

import io.netty.channel.*;
import io.netty.handler.codec.socksx.SocksMessage;
import io.netty.handler.codec.socksx.v5.*;
import org.pierce.UtilTools;
import org.pierce.bridge.ByteBufMessageBridge;
import org.pierce.list.entity.TryConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class SocksServerHandler extends ChannelInboundHandlerAdapter {

    //static NameListCheck check = LocalServer.getNameListCheck();

    private static final Logger log = LoggerFactory.getLogger(SocksServerHandler.class);

    ByteBufMessageBridge messageBridge = new ByteBufMessageBridge();

    public SocksServerHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        //log.info("{}", UtilTools.formatChannelInfo(ctx));
        if (message instanceof SocksMessage socksRequest) {
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
                    } else if (socksRequest instanceof Socks5CommandRequest socks5CmdRequest) {
                        //String mode = JproxyProperties.getProperty("socks.server.middle-mode");
                        if (socks5CmdRequest.type() == Socks5CommandType.CONNECT) {
                            String address = socks5CmdRequest.dstAddr();
                            int port = socks5CmdRequest.dstPort();
                            Channel channel = ctx.channel();

                            channel.attr(ByteBufMessageBridge.TARGET_ADDRESS).set(address);
                            channel.attr(ByteBufMessageBridge.TARGET_PORT).set(port);
                            messageBridge.bridge(ctx.channel(), new TryConnect()).addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture future) throws Exception {
                                    DefaultSocks5CommandResponse resp;
                                    if (future.isSuccess()) {
                                        resp = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
                                    } else {
                                        resp = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
                                    }
                                    ctx.writeAndFlush(resp).addListener(new ChannelFutureListener() {
                                        @Override
                                        public void operationComplete(ChannelFuture future) throws Exception {
                                            if (!ctx.channel().isActive()) {
                                                ctx.channel().close();
                                                return;
                                            }

                                            if (!ctx.isRemoved()) {
                                                ctx.pipeline().remove(Socks5CommandRequestDecoder.class);
                                                ctx.pipeline().remove(Socks5InitialRequestDecoder.class);
                                                ctx.pipeline().remove(Socks5ServerEncoder.class);
                                                //ctx.pipeline().remove(SocksServerHandler.class);

                                            }
                                            if (future.isSuccess()) {
                                                return;
                                            }
                                        }
                                    });
                                }
                            });
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
        messageBridge.bridge(ctx.channel(), message);

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        //log.info("{}", UtilTools.formatChannelInfo(ctx));
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


}
