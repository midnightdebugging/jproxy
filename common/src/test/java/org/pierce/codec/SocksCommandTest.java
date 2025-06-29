package org.pierce.codec;

import com.google.gson.Gson;
import org.junit.Test;

public class SocksCommandTest {

    @Test
    public void test001() {
        Gson gson = new Gson();
        SocksCommandDNSRequest request = new SocksCommandDNSRequest();
        request.setDomain("example.com");
        // 序列化
        String json = gson.toJson(request);

        System.out.println(json);


        // 反序列化
        SocksCommandDNSRequest request_new = gson.fromJson(json, SocksCommandDNSRequest.class);
        System.out.println(request_new.getDomain());

    }

    @Test
    public void test002() {
        Gson gson = new Gson();
        SocksCommandClose request = new SocksCommandClose();
        // 序列化
        String json = gson.toJson(request);

        System.out.println(json);


        // 反序列化
        SocksCommandClose request_new = gson.fromJson(json, SocksCommandClose.class);
        System.out.println(request_new);

    }

    @Test
    public void test003() {
        SocksCommandDNSRequest request = new SocksCommandDNSRequest();

        System.out.println(request.getClass().equals(SocksCommandDNSRequest.class));
    }
}
