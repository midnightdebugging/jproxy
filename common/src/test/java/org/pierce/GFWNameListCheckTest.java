package org.pierce;

import org.junit.Test;
import org.pierce.nlist.imp.GFWNameListCheck;

import java.io.IOException;

public class GFWNameListCheckTest {
    @Test
    public void test001() throws IOException {
        GFWNameListCheck gfwNameListCheck = new GFWNameListCheck() {
            {
                loadConfigure();
            }
        };
    }

    public void check(GFWNameListCheck gfwNameListCheck, String address, int port) {
        System.out.printf("%s:%d ==> %s\n", address, port, gfwNameListCheck.check(address, port));
    }

    @Test
    public void test002() {
        String gfwStr = "||addons.mozilla.org/*-*/firefox/addon/ublock-origin/*";
    }

}
