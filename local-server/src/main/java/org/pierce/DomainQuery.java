package org.pierce;

import io.netty.util.concurrent.Promise;

import java.util.List;

public interface DomainQuery {
    void query(String domain, Promise<List<String>> promise) throws Throwable;
}
