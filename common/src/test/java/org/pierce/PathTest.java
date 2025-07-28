package org.pierce;

import org.junit.Test;

import java.util.Properties;
import java.util.regex.Pattern;

public class PathTest {
    Pattern rootPathTest = Pattern.compile("^/|^[a-zA-Z]:\\\\");

    @Test
    public void test001() {
        String testStr = "/aa/bb/cc/dd";
        System.out.println(rootPathTest.matcher(testStr).find());

        testStr = "aa/bb/cc/dd";
        System.out.println(rootPathTest.matcher(testStr).find());

        testStr = "d:\\xx\\yy\\zz";
        System.out.println(rootPathTest.matcher(testStr).find());

        //System.out.println(JproxyProperties.getProperties());

        System.out.println(JproxyProperties.evaluate("${user.dir}/dd/xx/ff"));

    }
}
