package org.pierce.nlist.util;

public class NetTools {
    public static byte[] IP2Bytes(String address) {
        String[] strings = address.split("\\.");

        if (strings.length != 4) {
            throw new RuntimeException(String.format("Incorrect format：%s", address));
        }
        byte[] bytes = new byte[strings.length];
        bytes[0] = (byte) (0xff & Integer.parseInt(strings[0]));
        bytes[1] = (byte) (0xff & Integer.parseInt(strings[1]));
        bytes[2] = (byte) (0xff & Integer.parseInt(strings[2]));
        bytes[3] = (byte) (0xff & Integer.parseInt(strings[3]));

        return bytes;
    }

    public static int IP2Integer(String address) {
        byte[] bytes = IP2Bytes(address);
        if (bytes.length != 4) {
            throw new RuntimeException(String.format("Incorrect format：%s", address));
        }
        int ret;
        ret = (bytes[0] & 0xff) << 24;
        ret = ret | ((bytes[1] & 0xff) << 16);
        ret = ret | ((bytes[2] & 0xff) << 8);
        ret = ret | (bytes[3] & 0xff);
        return ret;
    }

    public static boolean sampleNet(String address1, String address2, int cidrLen) {
        int addressInt1 = IP2Integer(address1);
        int addressInt2 = IP2Integer(address2);
        int mask = 0x80000000;
        for (int i = 1; i < cidrLen; i++) {
            mask = mask >> 1;
        }
        return (addressInt1 & mask) == (addressInt2 & mask);
    }

    public static void main(String[] args) {
        String test = "192.168.0.125";
        byte[] bytes = IP2Bytes(test);
        for (byte aByte : bytes) {
            System.out.printf("byte:%x\n", aByte & 0xff);
        }
        System.out.printf("IP2Integer:%x\n", IP2Integer(test));
        System.out.printf("sampleNet:%b\n", sampleNet("192.168.0.125","192.168.128.125",16));
    }
}
