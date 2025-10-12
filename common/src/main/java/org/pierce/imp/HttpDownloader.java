package org.pierce.imp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;
import org.pierce.Downloader;
import org.pierce.Jproxy;
import org.pierce.UtilTools;
import org.pierce.entity.ProtocolInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpDownloader implements Downloader {

    private final static Logger log = LoggerFactory.getLogger(HttpDownloader.class);


    @Override
    public Promise<Channel> download(String url, String savePath, boolean proxy) {

        EventExecutor executor = ImmediateEventExecutor.INSTANCE;
        Promise<Channel> promise = executor.newPromise();
        ProtocolInfo protocolInfo = UtilTools.parseProtocolInfo(url);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(Jproxy.getEventLoopGroup());
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new HttpDownloaderInitializer(savePath, url, promise));
        ChannelFuture channelFuture = bootstrap.connect(protocolInfo.getHostAddress(), protocolInfo.getPort());
        channelFuture.addListener((ChannelFutureListener) channelFuture1 -> {
            if (channelFuture1.isSuccess()) {
                log.info("连接成功");
            } else {
                log.info("连接失败");
                promise.tryFailure(new RuntimeException("连接失败"));
            }
        });
        return promise;
    }


}
