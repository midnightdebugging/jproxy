package org.pierce.impl;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.util.concurrent.*;
import org.apache.ibatis.session.SqlSession;
import org.pierce.*;
import org.pierce.imp.MemoryTimeOutFailTryCheck;
import org.pierce.mybatis.mapper.HostName2AddressMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

public class DefaultDomainQuery {


    public final static String remoteAddress = JproxyProperties.getProperty("local-server.link-out.address");

    public final static int remotePort = Integer.parseInt(JproxyProperties.getProperty("local-server.remote-socks-link-out.port"));

    private static final Logger log = LoggerFactory.getLogger(DefaultDomainQuery.class);

    private static final HashMap<String, Integer> blackList0 = new HashMap<>();

    private final static FailTryCheck failTryCheck = new MemoryTimeOutFailTryCheck();
    static EventLoopGroup eventLoopGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

    static {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {

                log.info("eventLoopGroup.shutdownGracefully();");
                eventLoopGroup.shutdownGracefully();
            }
        });
    }

    public static void query(EventLoopGroup eventLoop, String domain, Promise<String> promise) {
        log.info("blackList0:{}", UtilTools.objToString(blackList0));

        if (!failTryCheck.check("dns-query/" + domain)) {
            promise.tryFailure(new RuntimeException("!failTryCheck.check(domain)"));
            return;
        }

        try (SqlSession sqlSession = DataBase.getSqlSessionFactory().openSession()) {

            HostName2AddressMapper mapper = sqlSession.getMapper(HostName2AddressMapper.class);
            List<String> stringList = mapper.selectAllAddressByHostName("remote-dns", domain);
            if (stringList != null && !stringList.isEmpty()) {
                String ipaddr = LocalServer.getInstance().getStringSelector().select(stringList);
                promise.trySuccess(ipaddr);
                return;
            }


        }
        EventExecutor executor = ImmediateEventExecutor.INSTANCE;

        DomainQuery domainQuery = new JproxyDomainQuery(eventLoopGroup);
        Promise<List<String>> jproxyPromise = executor.newPromise();
        jproxyPromise.addListener(new GenericFutureListener<Future<? super List<String>>>() {
            @Override
            public void operationComplete(Future<? super List<String>> future) throws Exception {
                if (future.isSuccess()) {
                    List<String> futureList = (List<String>) future.get();
                    for (String ip : futureList) {
                        LocalServer.getInstance().updateHostAddress("remote-dns", domain, ip);
                    }
                    try (SqlSession sqlSession = DataBase.getSqlSessionFactory().openSession()) {

                        HostName2AddressMapper mapper = sqlSession.getMapper(HostName2AddressMapper.class);
                        List<String> stringList = mapper.selectAllAddressByHostName("remote-dns", domain);
                        if (stringList != null && !stringList.isEmpty()) {
                            String ipaddr = LocalServer.getInstance().getStringSelector().select(stringList);
                            promise.trySuccess(ipaddr);
                            return;
                        }


                    }
                    return;
                }
                failTryCheck.failCount("dns-query/" + domain);
                promise.tryFailure(future.cause());
            }
        });
        try {
            domainQuery.query(domain, jproxyPromise);
        } catch (Throwable e) {
            failTryCheck.failCount("dns-query" + domain);
            promise.tryFailure(e);
        }

    }
}
