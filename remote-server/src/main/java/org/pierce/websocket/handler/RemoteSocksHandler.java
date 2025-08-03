package org.pierce.websocket.handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.pierce.UtilTools;
import org.pierce.codec.SocksCommand;
import org.pierce.codec.SocksCommandDNSRequest;
import org.pierce.codec.SocksCommandDNSResponse;
import org.pierce.codec.SocksCommandResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;


public class RemoteSocksHandler extends SimpleChannelInboundHandler<SocksCommand> {

    private static final Logger log = LoggerFactory.getLogger(RemoteSocksHandler.class);

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
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("{}", UtilTools.formatChannelInfo(ctx), cause);
        if (ctx.channel().isActive()) {
            ctx.channel().close();
        }
    }
}
