package org.pierce;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JproxyPropertiesTest {

    public static String evaluate(String testStr) {

        while (true) {

            Matcher matcher = pattern.matcher(testStr);
            if (!matcher.find()) {
                break;
            }
            int start = matcher.start();
            int end = matcher.end();

            String replace = JproxyProperties.getProperty(matcher.group(1), "");
            testStr = testStr.substring(0, start) + replace + testStr.substring(end);
        }
        return testStr;
    }


    public final static Pattern pattern = Pattern.compile("\\$\\{([^${}]+)}");

    public static void main(String[] args) {
        String testStr = "${user.home}/.jproxy";
        System.out.printf("%s==>%s\n", testStr, evaluate(testStr));
        System.out.println("====================");
        System.out.println(JproxyProperties.getProperty("test-path"));

    }
}
