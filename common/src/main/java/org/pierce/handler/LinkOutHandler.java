package org.pierce.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Promise;
import org.pierce.FailTryCheck;
import org.pierce.UtilTools;
import org.pierce.imp.MemeryFailTryCheck;
import org.pierce.session.SessionAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.Queue;


public class LinkOutHandler extends ChannelInboundHandlerAdapter {

    private final static Logger log = LoggerFactory.getLogger(LinkOutHandler.class);

    private final static FailTryCheck failTryCheck = new MemeryFailTryCheck();
    final Channel linkInChannel;
    //private final String address;
    //private final int port;
    protected Channel linkOutChannel;
    final Promise<Channel> promise;

    Queue<Object> queue = new LinkedList<>();

    String targetAddress;
    int targetPort;


    protected void initChannel(SocketChannel ch, Promise<Channel> promise) throws URISyntaxException {
        ch.pipeline().addLast(new DebugHandler("link-out"));
        ch.pipeline().addLast("LinkOutHandler-handler", new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

                linkOutRead(msg);
            }

            @Override
            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                log.info("{}", UtilTools.formatChannelInfo(ctx));
                if (linkOutChannel != null && linkOutChannel.isActive()) {
                    linkOutChannel.close();
                }
                if (linkInChannel != null && linkInChannel.isActive()) {
                    linkInChannel.close();
                }
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                log.info("{}", UtilTools.formatChannelInfo(ctx), cause);
                if (linkOutChannel != null && linkOutChannel.isActive()) {
                    linkOutChannel.close();
                }
                if (linkInChannel != null && linkInChannel.isActive()) {
                    linkInChannel.close();
                }
            }

            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                ctx.read();
            }
        });
    }

    public LinkOutHandler(Channel linkInChannel, Promise<Channel> promise, String address, int port) {
        this(linkInChannel, promise, address, port, null, 0);
    }

    public LinkOutHandler(Channel linkInChannel, Promise<Channel> promise, String address, int port, String targetAddress, int targetPort) {
        this.targetAddress = targetAddress;
        this.targetPort = targetPort;
        this.linkInChannel = linkInChannel;
        this.promise = promise;
        //this.address = address;
        //this.port = port;

        if (!failTryCheck.check(linkInChannel.localAddress() + "/" + address)) {
            if (linkInChannel.isActive()) {
                linkInChannel.close();
            }
            return;
        }
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(linkInChannel.eventLoop());
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    LinkOutHandler.this.initChannel(ch, promise);
                }

            });
            log.info("{} connect:{}:{} start", UtilTools.formatChannelInfo(linkInChannel), address, port);
            linkOutStatusEvent(linkInChannel, null, new LinkOutStatusEvent(LinkOutStep.CONNECT_START, true));
            ChannelFuture cf = bootstrap.connect(address, port);
            //cf.channel().attr(SessionAttributes.TARGET_PORT,)
            cf.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (!channelFuture.isSuccess()) {
                        failTryCheck.failCount(linkInChannel.localAddress() + "/" + address);
                        log.info("{} connect:{}:{} failure", UtilTools.formatChannelInfo(linkInChannel), address, port);
                        linkOutStatusEvent(linkInChannel, null, new LinkOutStatusEvent(LinkOutStep.CONNECT_FINISH, false, new Throwable(channelFuture.cause())));
                        if (linkOutChannel != null && linkOutChannel.isActive()) {
                            linkOutChannel.close();
                        }
                        if (linkInChannel.isActive()) {
                            linkInChannel.close();
                        }

                        return;
                    }
                    linkOutChannel = channelFuture.channel();
                    linkOutStatusEvent(linkInChannel, linkOutChannel, new LinkOutStatusEvent(LinkOutStep.CONNECT_FINISH, true));
                    linkOutChannel.read();
                    log.info("{} connect:{}:{} success,{}", UtilTools.formatChannelInfo(linkInChannel), address, port, UtilTools.formatChannelInfo(channelFuture.channel()));
                }
            });
        } catch (Exception e) {
            log.info("{} connect:{}:{} Exception", UtilTools.formatChannelInfo(linkInChannel), address, port);
            linkOutStatusEvent(linkInChannel, null, new LinkOutStatusEvent(LinkOutStep.CONNECT_START, false, new Throwable(e)));

            if (linkOutChannel != null && linkOutChannel.isActive()) {
                linkOutChannel.close();
            }
            if (linkInChannel.isActive()) {
                linkInChannel.close();
            }

        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (linkOutChannel == null) {
            queue.add(msg);
            return;
        }
        linkInRead(msg);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("{}", UtilTools.formatChannelInfo(ctx));
        if (linkOutChannel != null && linkOutChannel.isActive()) {
            linkOutChannel.close();
        }
        if (linkInChannel != null && linkInChannel.isActive()) {
            linkInChannel.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("{}", UtilTools.formatChannelInfo(ctx), cause);
        if (linkOutChannel != null && linkOutChannel.isActive()) {
            linkOutChannel.close();
        }
        if (linkInChannel != null && linkInChannel.isActive()) {
            linkInChannel.close();
        }
    }

    public void linkOutStatusEvent(LinkOutStatusEvent linkOutStatusEvent) {
        linkOutStatusEvent(this.linkInChannel, this.linkOutChannel, linkOutStatusEvent);
    }

    public void linkOutStatusEvent(Channel linkIn, Channel linkOut, LinkOutStatusEvent linkOutStatusEvent) {

        if (!linkOutStatusEvent.isSuccess()) {
            log.info("{} {} ", UtilTools.formatChannelInfo(linkIn), UtilTools.objToString(linkOutStatusEvent), linkOutStatusEvent.getCause());
            this.promise.tryFailure(linkOutStatusEvent.getCause());
            return;
        }
        log.info("{} {} ", UtilTools.formatChannelInfo(linkIn), UtilTools.objToString(linkOutStatusEvent));
        log.info("queue.size():{}", String.valueOf(queue.size()));
        if (linkOutStatusEvent.getLinkOutStep() == LinkOutStep.CONNECT_FINISH) {

            while (!queue.isEmpty()) {
                Object msg = queue.remove();
                log.info(msg.toString());
                linkInRead(msg);

            }
            linkIn.attr(SessionAttributes.OVER_ADDRESS).set(linkOut.remoteAddress().toString());
            this.promise.setSuccess(linkOut);
        }
    }

    public void linkInRead(Object msg) {
        log.debug("{} {}", UtilTools.formatChannelInfo(linkInChannel), msg.getClass());
        linkOutChannel.writeAndFlush(msg);
    }

    public void linkOutRead(Object msg) {
        log.debug("{} {}", UtilTools.formatChannelInfo(linkOutChannel), msg.getClass());
        //log.info("{}", msg.getClass());
        linkInChannel.writeAndFlush(msg);
    }


    public Channel getLinkInChannel() {
        return linkInChannel;
    }

    public Channel getLinkOutChannel() {
        return linkOutChannel;
    }
}
