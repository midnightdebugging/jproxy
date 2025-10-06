package org.pierce.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.pierce.Jproxy;
import org.pierce.UtilTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class NettyChannelPoolManager implements AutoCloseable {


    private static final Logger log = LoggerFactory.getLogger(NettyChannelPoolManager.class);


    public static final AttributeKey<ChannelPool> CHANNEL_POOL = AttributeKey.valueOf("CHANNEL_POOL");

    Map<String, ChannelPool> channelPoolMap = new HashMap<>();

    ChannelInitializer<Channel> channelInitializer;

    public NettyChannelPoolManager(ChannelInitializer<Channel> channelInitializer) {
        this.channelInitializer = channelInitializer;
    }


    private ChannelPool getChannelPool(String address, int port) {
        String newKey = String.format("[%s]:%d", address, port);

        synchronized (this) {
            if (!channelPoolMap.containsKey(newKey)) {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(Jproxy.getEventLoopGroup())
                        .channel(NioSocketChannel.class)
                        .remoteAddress(new InetSocketAddress(address, port));
                FixedChannelPool channelPool = new FixedChannelPool(bootstrap, poolHandler, 16);
                channelPoolMap.put(newKey, channelPool);


            }

        }

        return channelPoolMap.get(newKey);
    }

    private final ChannelPoolHandler poolHandler = new ChannelPoolHandler() {

        @Override
        public void channelReleased(Channel ch) throws Exception {

        }

        @Override
        public void channelAcquired(Channel ch) throws Exception {

        }

        @Override
        public void channelCreated(Channel channel) throws Exception {
            channel.pipeline().addLast(channelInitializer);
        }
    };

    public Future<Channel> acquire(String address, int port) {
        log.debug("{}:{}", address, port);
        ChannelPool channelPool = getChannelPool(address, port);
        Future<Channel> channelFuture = channelPool.acquire();
        channelFuture.addListener((GenericFutureListener<Future<Channel>>) future -> {
            if (future.isSuccess()) {
                log.debug("{} {}:{}", UtilTools.formatChannelInfo(future.get()), address, port);
                Channel channel = future.getNow();
                channel.attr(CHANNEL_POOL).set(channelPool);
            }
        });
        return channelFuture;
    }

    public static Future<Void> release(Channel channel) {
        log.debug("{}", UtilTools.formatChannelInfo(channel));
        return channel.attr(CHANNEL_POOL).get().release(channel);
    }

    @Override
    public synchronized void close() throws Exception {
        for (Map.Entry<String, ChannelPool> entry : channelPoolMap.entrySet()) {
            try {
                entry.getValue().close();
            } catch (Throwable t) {
                log.info("Throwable", t);
            }

        }
        channelPoolMap.clear();
    }


}
