package org.pierce.imp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.pierce.ChannelPool;
import org.pierce.UnderTest;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

@UnderTest
public class DefaultChannelPool implements ChannelPool {

    private final Bootstrap bootstrap;
    private final Queue<Channel> pool = new LinkedList<>();
    private final int maxSize;
    private int createdCount = 0;

    public DefaultChannelPool(Bootstrap bootstrap, int maxSize) {
        this.bootstrap = bootstrap.clone();
        this.maxSize = maxSize;
    }

    @Override
    public synchronized CompletableFuture<Channel> acquire() {

        CompletableFuture<Channel> future = new CompletableFuture<>();
        while (!pool.isEmpty()) {
            Channel channel = pool.remove();
            if (!channel.isActive()) {
                //丢弃
                createdCount--;
                continue;
            }
            future.complete(channel);
            return future;
        }


        // 创建新连接（不超过最大限制）
        if (createdCount < maxSize) {
            createdCount++;
            newChanel(future);
        } else {
            future.completeExceptionally(new IllegalStateException("Connection pool exhausted"));
        }
        return future;
    }


    @Override
    public synchronized void release(Channel channel) {
        if (channel.isActive()) {
            pool.offer(channel); // 归还可用连接
        } else {
            channel.close();     // 关闭失效连接
            createdCount--;
        }
    }

    @Override
    public void close() {
        pool.forEach(Channel::close);
        pool.clear();
    }

    private void newChanel(CompletableFuture<Channel> future) {
        bootstrap.connect().addListener((ChannelFuture connectFuture) -> {
            if (connectFuture.isSuccess()) {
                future.complete(connectFuture.channel());
            } else {
                synchronized (DefaultChannelPool.this) {
                    createdCount--;
                }
                future.completeExceptionally(connectFuture.cause());
            }
        });
    }

}
