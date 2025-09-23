package org.pierce.socks;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.util.concurrent.*;
import org.pierce.impl.JproxyDomainQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class JproxyDomainQueryTest {

    private static final Logger log = LoggerFactory.getLogger(JproxyDomainQueryTest.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException, URISyntaxException {
        EventLoopGroup eventLoopGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {

                log.info("eventLoopGroup.shutdownGracefully();");
                eventLoopGroup.shutdownGracefully();
            }
        });

        String[] hostNameArr = new String[]{
                "github.com"
        };
        EventExecutor executor = ImmediateEventExecutor.INSTANCE;
        for (String hostName : hostNameArr) {
            Promise<List<String>> promise0 = executor.newPromise();
            promise0.addListener(new GenericFutureListener<Future<? super List<String>>>() {
                @Override
                public void operationComplete(Future<? super List<String>> future) throws Exception {
                    if (future.isSuccess()) {
                        log.info("{} == > {}", hostName, future.getNow());
                        return;
                    }
                    log.info("{} fail", hostName);
                }
            });
            JproxyDomainQuery jproxyDomainQuery = new JproxyDomainQuery(eventLoopGroup);
            jproxyDomainQuery.query(hostName, promise0);
        }


/*        try {
            Thread.sleep(1000 * 30);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/
    }
}
