package org.pierce.list.imp;

import org.junit.Test;
import org.pierce.Jproxy;
import org.pierce.list.NameListCheck;

public class TextNameListCheckTest {
    @Test
    public void test001() {
        Jproxy.getInstance().initialize(TextNameListCheckTest.class, false);
        TextNameListCheck textNameListCheck = new TextNameListCheck();
        textNameListCheck.loadByInputStream();
        printfCheck(textNameListCheck, "example.com");
        printfCheck(textNameListCheck, "2607:f810:4007:080f:0000:0000:0000:2003");
        printfCheck(textNameListCheck, "2607:f810:4007:80f::2003");
        printfCheck(textNameListCheck, "2607:f810:4007:80f::2004");
    }

    public void printfCheck(NameListCheck textNameListCheck, String address) {
        System.out.printf("%s ==> %s\n", address, textNameListCheck.check(address, -1));
    }
}
