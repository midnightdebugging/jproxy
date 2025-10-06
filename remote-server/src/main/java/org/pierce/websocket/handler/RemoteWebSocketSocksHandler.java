package org.pierce.websocket.handler;

import io.netty.channel.*;
import org.pierce.MessageBridge;
import org.pierce.UtilTools;
import org.pierce.codec.SocksCommand;
import org.pierce.codec.SocksCommandDNSRequest;
import org.pierce.codec.SocksCommandDNSResponse;
import org.pierce.codec.SocksCommandResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;


public class RemoteWebSocketSocksHandler extends SimpleChannelInboundHandler<SocksCommand> {

    private static final Logger log = LoggerFactory.getLogger(RemoteWebSocketSocksHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocksCommand msg) throws Exception {

        if (msg instanceof SocksCommandDNSRequest) {

            ctx.channel().eventLoop().execute(new Runnable() {
                @Override
                public void run() {
                    SocksCommandDNSResponse resp = new SocksCommandDNSResponse();
                    resp.setDomain(((SocksCommandDNSRequest) msg).getDomain());
                    try {
                        InetAddress[] addresses = InetAddress.getAllByName(((SocksCommandDNSRequest) msg).getDomain());
                        for (InetAddress inetAddress : addresses) {
                            resp.addIp(inetAddress.getHostAddress());
                        }
                        resp.setCode(SocksCommandResponseCode.SUCCESS);
                    } catch (Exception e) {
                        log.warn("DNS query:{} fail", ((SocksCommandDNSRequest) msg).getDomain(), e);
                        resp.setCode(SocksCommandResponseCode.FAIL);
                    } finally {

                    }

                    ctx.writeAndFlush(resp).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (!msg.isKeep()) {
                                log.info("ctx.channel().close();");
                                ctx.channel().close();
                            }
                        }
                    });
                }
            });

            return;
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("{}", UtilTools.formatChannelInfo(ctx));
        Channel linkOut = ctx.channel().attr(MessageBridge.LINK_OUT).get();
        if (linkOut != null) {
            linkOut.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("{}", UtilTools.formatChannelInfo(ctx), cause);
        if (ctx.channel().isActive()) {
            ctx.channel().close();
        }
    }
}
