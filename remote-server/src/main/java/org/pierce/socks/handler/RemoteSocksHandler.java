package org.pierce.socks.handler;

import io.netty.channel.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.pierce.RemoteServer;
import org.pierce.UtilTools;
import org.pierce.codec.*;
import org.pierce.handler.LinkOutHandler;
import org.pierce.nlist.Directive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class RemoteSocksHandler extends SimpleChannelInboundHandler<SocksCommand> {

    private static final Logger log = LoggerFactory.getLogger(RemoteSocksHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocksCommand msg) throws Exception {


        if (msg instanceof SocksCommandConnectRequest) {
            String address = ((SocksCommandConnectRequest) msg).getTarget();
            int port = ((SocksCommandConnectRequest) msg).getPort();
            Directive directive = RemoteServer.getNameListCheck().check(address, port);
            if (directive == Directive.DISALLOW_CONNECT) {
                SocksCommandConnectResponse resp = new SocksCommandConnectResponse();
                resp.setCode(SocksCommandResponseCode.DISALLOW_CONNECT);
                ctx.writeAndFlush(resp).addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        if (ctx.channel().isActive()) {
                            ctx.channel().close();
                        }
                    }
                });
                return;
            }


            Promise<Channel> promise = ctx.executor().newPromise();
            promise.addListener(future -> {
                SocksCommandConnectResponse resp = new SocksCommandConnectResponse();

                if (future.isSuccess()) {
                    resp.setCode(SocksCommandResponseCode.SUCCESS);
                } else {
                    resp.setCode(SocksCommandResponseCode.FAIL);
                }

                ChannelFuture cf = ctx.writeAndFlush(resp);
                cf.addListener(future1 -> {
                    if (ctx.channel().isActive()) {
                        ctx.pipeline().remove(SocksCommandCodec.class);
                        ctx.pipeline().remove(RemoteSocksHandler.this);
                    }

                });

            });


            ctx.channel().eventLoop().execute(() -> {
                try {
                    InetAddress inetAddress = InetAddress.getByName(address);
                    String newAddress = inetAddress.getHostAddress();
                    Directive directive1 = RemoteServer.getNameListCheck().check(address, port);
                    if (directive1 == Directive.DISALLOW_CONNECT) {
                        SocksCommandConnectResponse resp = new SocksCommandConnectResponse();
                        resp.setCode(SocksCommandResponseCode.DISALLOW_CONNECT);
                        ctx.writeAndFlush(resp).addListener(future -> {
                            if (ctx.channel().isActive()) {
                                ctx.channel().close();
                            }
                        });
                        return;
                    }
                    ctx.pipeline().addLast(new LinkOutHandler(ctx.channel(), promise, newAddress, port));
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            });


            return;
        }
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
