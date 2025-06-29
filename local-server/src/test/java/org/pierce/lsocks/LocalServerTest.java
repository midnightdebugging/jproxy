package org.pierce.lsocks;

import org.junit.Before;
import org.junit.Test;
import org.pierce.LocalServer;
import org.pierce.nlist.NameListCheck;

public class LocalServerTest {

    @Before
    public void initial() throws ClassNotFoundException {
        LocalServer.getInstance().initialize();
    }
    public void test001(NameListCheck check, String input) {
        System.out.printf("%s ==> %s\n", input, check.check(input));
    }
    @Test
    public void test001(){
        NameListCheck check = LocalServer.getInstance().getNameListCheck();
        test001(check, "google.com");
        test001(check, "xyz.example.com");
        test001(check, "google.com");
        test001(check, "google.com");
        test001(check, "google.com");
        test001(check, "aa.google.com");
        test001(check, "dl.google.com");
        test001(check, "missav.ai");
        test001(check, "xxx.missav.ai");
        test001(check, "127.2.0.1");
        test001(check, "10.0.0.1");
        test001(check, "192.168.128.1");
        test001(check, "1.2.128.1");
    }
}
