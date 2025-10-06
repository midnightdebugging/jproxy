package org.pierce.impl;

import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.*;
import org.pierce.ConnectionTypeCheck;
import org.pierce.list.Directive;
import org.pierce.list.NameListCheck;
import org.pierce.list.entity.ConnectType;
import org.pierce.session.SessionAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DefaultConnectionTypeCheck implements ConnectionTypeCheck {


    private static final Logger log = LoggerFactory.getLogger(DefaultConnectionTypeCheck.class);

    //final static NameListCheck check = LocalServer.getInstance().getNameListCheck();

    final static Map<String, NameListCheck> checkMap = new HashMap<String, NameListCheck>();

    @Override
    public void check(Channel channel, String address, int targetPort, Promise<ConnectType> promise) {

        EventLoop eventLoop = channel.eventLoop();

        NameListCheck nameListCheck = channel.attr(SessionAttributes.NAME_LIST_CHECK).get();

        Directive directive = nameListCheck.check(address, targetPort);

        log.info("{} {}", directive, address);
        if (directive == Directive.DISALLOW_CONNECT) {
            promise.setSuccess(new ConnectType(directive, address));
        } else if (directive == Directive.DIRECT_CONNECT) {
            promise.setSuccess(new ConnectType(directive, address));
        } else if (directive == Directive.FULL_CONNECT) {
            promise.setSuccess(new ConnectType(directive, address));
        } else if (directive == Directive.DOMAIN_NAME_QUERY_FIRST) {


            EventExecutor executor = ImmediateEventExecutor.INSTANCE;
            Promise<String> stringPromise = new DefaultPromise<>(executor);

            stringPromise.addListener(new GenericFutureListener<Future<? super String>>() {
                @Override
                public void operationComplete(Future<? super String> future) throws Exception {
                    if (future.isSuccess()) {
                        String newAddress = String.valueOf(future.getNow());
                        check(channel, newAddress, targetPort, promise);
                        return;
                    }
                    promise.setFailure(new RuntimeException(String.format("domain query %s fail", address)));

                }
            });

            DefaultDomainQuery.query(eventLoop, address, stringPromise);


        }

    }

    public Promise<ConnectType> check(Channel channel, String address, int targetPort) {
        EventExecutor executor = ImmediateEventExecutor.INSTANCE;
        Promise<ConnectType> promise = new DefaultPromise<>(executor);
        check(channel, address, targetPort, promise);
        return promise;
    }

}
