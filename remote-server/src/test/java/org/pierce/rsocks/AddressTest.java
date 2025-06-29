package org.pierce.rsocks;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AddressTest {
    public static void main(String[] args) throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getByName("example.com");
        System.out.println(inetAddress.getHostAddress());

        inetAddress = InetAddress.getByName("182.61.201.211");
        System.out.println(inetAddress.getHostAddress());

        inetAddress = InetAddress.getByName("2606:4700:3033:0:0:0:ac43:8d40");
        System.out.println(inetAddress.getHostAddress());
    }
}
