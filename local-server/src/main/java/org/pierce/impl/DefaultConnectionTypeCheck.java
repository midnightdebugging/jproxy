package org.pierce.impl;

import io.netty.channel.EventLoop;
import io.netty.util.concurrent.*;
import org.pierce.ConnectionTypeCheck;
import org.pierce.LocalServer;
import org.pierce.entity.ConnectType;
import org.pierce.nlist.Directive;
import org.pierce.nlist.NameListCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConnectionTypeCheck implements ConnectionTypeCheck {


    private static final Logger log = LoggerFactory.getLogger(DefaultConnectionTypeCheck.class);

    final static NameListCheck check = LocalServer.getInstance().getNameListCheck();


    @Override
    public void check(EventLoop eventLoop, String address, Promise<ConnectType> promise) {


        Directive directive = check.check(address, Directive.DIRECT_CONNECT);
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
                        check(eventLoop, newAddress, promise);
                        return;
                    }
                    promise.setFailure(new RuntimeException(String.format("domain query %s fail", address)));

                }
            });

            DefaultDomainQuery.query(eventLoop, address, stringPromise);


        }

    }

}
