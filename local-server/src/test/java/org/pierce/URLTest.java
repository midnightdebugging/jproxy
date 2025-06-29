package org.pierce;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLTest {
    //Pattern pattern1 = Pattern.compile("^([^:/]+://)?([^:/]+)(?::([\\d/]+))?");
    //Pattern pattern2 = Pattern.compile("^([^/\\[\\]]+//)?(\\[[^/]+])(:\\d+)?");

/*    Pattern pattern1 = Pattern.compile("^([^:/]+://)?([^:/]+)(?::([\\d/]+))?(/|$)");
    Pattern pattern2 = pattern1;*/

    Pattern protocolPattern = Pattern.compile("^[a-z]+://");

    Pattern hostPortPattern = Pattern.compile("^(?:([^:/]+)|(\\[[^/]+]))(:\\d+)?(/|$)");

    //Pattern port = Pattern.compile("^:\\d+(/|$)");

    @Test
    public void test001() {
        testURL0("192.168.31.129", null, "192.168.31.129", null);
        testURL0("192.168.31.129:443", null, "192.168.31.129", ":443");
        testURL0("192.168.31.129:443/", null, "192.168.31.129", ":443");
        testURL0("https://example.com", "https://", "example.com", null);
        testURL0("https://example.com:443", "https://", "example.com", ":443");
        testURL0("https://example.com:443/", "https://", "example.com", ":443");
        testURL0("https://192.168.31.129", "https://", "192.168.31.129", null);
        testURL0("https://192.168.31.129:443", "https://", "192.168.31.129", ":443");
        testURL0("https://192.168.31.129:443/", "https://", "192.168.31.129", ":443");
        testURL0("[fd00:6868:6868:0:89eb:5de4:2477:257]:8080", null, "[fd00:6868:6868:0:89eb:5de4:2477:257]", ":8080");
        testURL0("sftp://[fd00:6868:6868:0:89eb:5de4:2477:257]:8080", "sftp://", "[fd00:6868:6868:0:89eb:5de4:2477:257]", ":8080");
        testURL0("https://[fd00:6868:6868:0:89eb:5de4:2477:257]:8080", "https://", "[fd00:6868:6868:0:89eb:5de4:2477:257]", ":8080");
        testURL0("https://[fd00:6868:6868:0:89eb:5de4:2477:257]", "https://", "[fd00:6868:6868:0:89eb:5de4:2477:257]", null);
    }

    @Test
    public void test002() {
        testURL1("192.168.31.129", null, "192.168.31.129", null);
        testURL1("192.168.31.129:443", null, "192.168.31.129", ":443");
        testURL1("192.168.31.129:443/", null, "192.168.31.129", ":443");
        testURL1("https://example.com", "https://", "example.com", null);
        testURL1("https://example.com:443", "https://", "example.com", ":443");
        testURL1("https://example.com:443/", "https://", "example.com", ":443");
        testURL1("http://example.com", "https://", "example.com", null);
        testURL1("http://example.com:443", "https://", "example.com", ":443");
        testURL1("http://example.com:443/", "https://", "example.com", ":443");

        testURL1("https://192.168.31.129", "https://", "192.168.31.129", null);
        testURL1("https://192.168.31.129:443", "https://", "192.168.31.129", ":443");
        testURL1("https://192.168.31.129:443/", "https://", "192.168.31.129", ":443");
        testURL1("[fd00:6868:6868:0:89eb:5de4:2477:257]:8080", null, "[fd00:6868:6868:0:89eb:5de4:2477:257]", ":8080");
        testURL1("sftp://[fd00:6868:6868:0:89eb:5de4:2477:257]:8080", "sftp://", "[fd00:6868:6868:0:89eb:5de4:2477:257]", ":8080");
        testURL1("https://[fd00:6868:6868:0:89eb:5de4:2477:257]:8080", "https://", "[fd00:6868:6868:0:89eb:5de4:2477:257]", ":8080");
        testURL1("https://[fd00:6868:6868:0:89eb:5de4:2477:257]", "https://", "[fd00:6868:6868:0:89eb:5de4:2477:257]", null);
    }

    public void testURL0(String input, String protocol, String hostName, String port) {


        String protocolStr = null;
        String hostNameStr = null;
        String portStr = null;


        System.out.println("====================================================");
        System.out.printf("input:%s\n", input);
        Matcher matcher = protocolPattern.matcher(input);
        if (matcher.find()) {
            protocolStr = input.substring(matcher.start(), matcher.end());
            System.out.printf("protocolStr:%s\n", protocolStr);
            input = input.substring(matcher.end());
        }

        matcher = hostPortPattern.matcher(input);

        if (matcher.find()) {
            hostNameStr = matcher.group(1);
            if (hostNameStr == null) {
                hostNameStr = matcher.group(2);
            }
            portStr = matcher.group(3);
            System.out.printf("host:%s\n", hostNameStr);
            System.out.printf("port:%s\n", portStr);
        }
        assert equals(protocolStr, protocol);
        assert equals(hostNameStr, hostName);
        assert equals(portStr, port);
    }

    public void testURL1(String input, String protocol, String hostName, String port) {
        System.out.printf("%s==>%s\n", input, UtilTools.objToString(UtilTools.parseProtocolInfo(input)));
    }
    /*public void test002(String testStr, String protocolPattern, String host, String port) {
        System.out.println("====================================================");
        System.out.println(testStr);
        Matcher matcher1 = pattern1.matcher(testStr);
        if (matcher1.find()) {
            System.out.printf("group1:%s\n", matcher1.group(1));
            System.out.printf("group2:%s\n", matcher1.group(2));
            System.out.printf("group3:%s\n", matcher1.group(3));
            if (!equals(protocolPattern, matcher1.group(1))) {
                throw new RuntimeException("!equals(protocolPattern,matcher1.group(1))");
            }

            if (!equals(host, matcher1.group(2))) {
                throw new RuntimeException("!equals(protocolPattern,matcher1.group(1))");
            }
            if (!equals(port, matcher1.group(3))) {
                throw new RuntimeException("!equals(protocolPattern,matcher1.group(1))");
            }
        } else {
            throw new RuntimeException("!matcher1.find()");
        }
    }

    public void test003(String testStr) {
        System.out.println("====================================================");
        System.out.println(testStr);
        Matcher matcher1 = pattern2.matcher(testStr);
        if (matcher1.find()) {
            System.out.printf("group1:%s\n", matcher1.group(1));
            System.out.printf("group2:%s\n", matcher1.group(2));
            System.out.printf("group3:%s\n", matcher1.group(3));
        }
    }*/

    boolean equals(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        }
        return str1.equals(str2);

    }
}
