package org.pierce.rsocks;

import org.junit.Test;
import org.pierce.RemoteServer;
import org.pierce.nlist.NameListCheck;

public class NameTest {
    @Test
    public void tets001() {
        NameListCheck nameListCheck = RemoteServer.getNameListCheck();
        System.out.println(nameListCheck.check("192.168.31.129"));
        System.out.println(nameListCheck.check("127.0.0.1"));
    }
}
