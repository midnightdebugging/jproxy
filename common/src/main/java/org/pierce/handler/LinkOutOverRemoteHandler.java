package org.pierce.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.Promise;
import org.pierce.JproxyProperties;
import org.pierce.UtilTools;
import org.pierce.codec.SocksCommandCodec;
import org.pierce.codec.SocksCommandConnectRequest;
import org.pierce.codec.SocksCommandConnectResponse;
import org.pierce.codec.SocksCommandResponseCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LinkOutOverRemoteHandler extends LinkOutHandler {

    final static Logger log = LoggerFactory.getLogger(LinkOutOverRemoteHandler.class);

    String targetAddress;
    int targetPort;

    final static String address = JproxyProperties.getProperty("local-server.link-out.address");

    final static int port = Integer.parseInt(JproxyProperties.getProperty("local-server.link-out.port"));

    public LinkOutOverRemoteHandler(Channel linkInChannel, Promise<Channel> promise, String targetAddress, int targetPort) {
        super(linkInChannel, promise, address, port);
        this.targetAddress = targetAddress;
        this.targetPort = targetPort;
    }

    @Override
    protected void initChannel(SocketChannel ch, Promise<Channel> promise) {

        if (JproxyProperties.booleanVal("tls-debug")) {
            ch.pipeline().addLast(new DebugHandler("tls-link-out"));
        }
        if (JproxyProperties.booleanVal("local-server.link-out.tls")) {
            ch.pipeline().addLast(TlsClientHandlerBuilder.getInstance().build(ch));
        }
        ch.pipeline().addLast(new DebugHandler("link-out"));
        ch.pipeline().addLast(new SocksCommandCodec());
        ch.pipeline().addLast("LinkOutOverRemoteHandler-biz", new ChannelInboundHandlerAdapter() {

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                if (msg instanceof SocksCommandConnectResponse) {

                    ch.pipeline().remove(SocksCommandCodec.class);

                    SocksCommandResponseCode code = ((SocksCommandConnectResponse) msg).getCode();
                    if (code == SocksCommandResponseCode.SUCCESS) {
                        linkOutStatusEvent(linkInChannel, linkOutChannel, new LinkOutStatusEvent(LinkOutStep.CONNECT_REMOTE_FINISH, true));
                    } else {
                        linkOutStatusEvent(linkInChannel, null, new LinkOutStatusEvent(LinkOutStep.CONNECT_REMOTE_FINISH, false, new RuntimeException(msg.getClass().getName() + ":" + UtilTools.objToString(msg))));
                    }
                    return;
                }

                linkInChannel.writeAndFlush(msg);
            }

            @Override
            public void channelActive(ChannelHandlerContext ctx) {
                linkOutStatusEvent(linkInChannel, linkOutChannel, new LinkOutStatusEvent(LinkOutStep.CONNECT_REMOTE_START, true));
                SocksCommandConnectRequest request1 = new SocksCommandConnectRequest();
                request1.setTarget(LinkOutOverRemoteHandler.this.targetAddress);
                request1.setPort(LinkOutOverRemoteHandler.this.targetPort);
                ctx.write(request1);
                ctx.flush();
            }

            @Override
            public void channelInactive(ChannelHandlerContext ctx) {
                log.info("{}", UtilTools.formatChannelInfo(ctx));
                if (linkOutChannel != null && linkOutChannel.isActive()) {
                    linkOutChannel.close();
                }
                if (linkInChannel != null && linkInChannel.isActive()) {
                    linkInChannel.close();
                }
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                log.info("{}", UtilTools.formatChannelInfo(ctx), cause);
                if (linkOutChannel != null && linkOutChannel.isActive()) {
                    linkOutChannel.close();
                }
                if (linkInChannel != null && linkInChannel.isActive()) {
                    linkInChannel.close();
                }
            }
        });
    }

    @Override
    public void linkOutStatusEvent(Channel linkIn, Channel linkOut, LinkOutStatusEvent linkOutStatusEvent) {
        log.info("{} {} ", UtilTools.formatChannelInfo(linkIn), UtilTools.objToString(linkOutStatusEvent));
        if (!linkOutStatusEvent.isSuccess()) {
            this.promise.tryFailure(linkOutStatusEvent.getCause());
            return;
        }
        if (linkOutStatusEvent.getLinkOutStep() == LinkOutStep.CONNECT_REMOTE_FINISH) {

            while (!queue.isEmpty()) {
                Object msg = queue.remove();
                log.info(String.valueOf(msg));
                linkInRead(msg);
            }
            this.promise.setSuccess(linkOut);
        }
    }
}
