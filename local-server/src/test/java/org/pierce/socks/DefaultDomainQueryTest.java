package org.pierce.socks;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.util.concurrent.*;
import org.junit.Before;
import org.junit.Test;
import org.pierce.LocalServer;
import org.pierce.impl.DefaultDomainQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultDomainQueryTest {

    @Before
    public void initial() throws ClassNotFoundException {
        LocalServer.getInstance().initialize();
    }

    private static final Logger log = LoggerFactory.getLogger(DefaultDomainQueryTest.class);

    @Test
    public void test001() {
        String domain = "google.com";
        EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        try {

            EventExecutor executor = ImmediateEventExecutor.INSTANCE;
            Promise<String> promise = getStringPromise(executor, workerGroup, domain);
            DefaultDomainQuery.query(workerGroup, domain, promise);
        } catch (Throwable t) {
            workerGroup.shutdownGracefully();
            log.info("DomainQuery.query fail", t);
        } finally {

        }
    }

    private static Promise<String> getStringPromise(EventExecutor executor, EventLoopGroup workerGroup, String domain) {
        Promise<String> promise = new DefaultPromise<>(executor);

        promise.addListener(new GenericFutureListener<Future<? super String>>() {
            @Override
            public void operationComplete(Future<? super String> future) throws Exception {
                workerGroup.shutdownGracefully();
                if (future.isSuccess()) {
                    log.info("{} == > {}", domain, future.getNow());
                    return;
                }
                log.info("DomainQuery.query fail");
            }
        });
        return promise;
    }

    public static void main(String[] args) throws ClassNotFoundException {
        LocalServer.getInstance().initialize();
        String domain = "google.com";
        EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        try {

            EventExecutor executor = ImmediateEventExecutor.INSTANCE;
            Promise<String> promise = getStringPromise(executor, workerGroup, domain);
            DefaultDomainQuery.query(workerGroup, domain, promise);
        } catch (Throwable t) {
            workerGroup.shutdownGracefully();
            log.info("DomainQuery.query fail", t);
        } finally {

        }
    }
}
