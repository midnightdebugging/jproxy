package org.pierce.lsocks.org.pierce.lsocks.db.mapper;

import org.junit.Test;
import org.pierce.nlist.NameListCheck;
import org.pierce.nlist.imp.TextNameListCheck;

import java.io.IOException;
import java.io.InputStream;


public class NameEntityTest {

    @Test
    public void test001() {
        try (InputStream is = NameEntityTest.class.getResourceAsStream("/name-list.txt")) {
            NameListCheck check = new TextNameListCheck(is);
            test001(check, "192.168.128.1");
            test001(check, "1.2.128.1");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void test001(NameListCheck check, String input) {
        System.out.printf("%s ==> %s\n", input, check.check(input));
    }

    @Test
    public void test002() {
    }

    @Test
    public void test003() {

        int integer = 0x80000000;
        System.out.printf("%x\n", integer >> 1);


    }
}
