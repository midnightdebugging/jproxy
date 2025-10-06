package org.pierce;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPv6Expander {
    public static void main(String[] args) {
        String compressedIP = "2607:f8b0:4007:80f::2003";
        try {
            // 解析IPv6地址
            InetAddress address = InetAddress.getByName(compressedIP);
            // 获取展开后的地址（自动填充省略的零）
            String expanded = address.getHostAddress();

            // 分割每个块并格式化为4位十六进制
            String[] blocks = expanded.split(":");
            StringBuilder formatted = new StringBuilder();
            for (String block : blocks) {
                // 每个块补前导零至4位
                String formattedBlock = String.format("%4s", block).replace(' ', '0');
                formatted.append(formattedBlock).append(":");
            }
            // 移除末尾多余的冒号
            formatted.setLength(formatted.length() - 1);

            System.out.println("Expanded IPv6: " + formatted);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
