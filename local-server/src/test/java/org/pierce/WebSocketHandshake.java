package org.pierce;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class WebSocketHandshake {
    // WebSocket握手使用的GUID
    private static final String MAGIC_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    public static String generateWebSocketAccept(String key) {
        try {
            // 拼接客户端密钥和GUID
            String input = key + MAGIC_GUID;

            // 获取SHA-1实例
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

            // 计算哈希值
            byte[] hash = sha1.digest(input.getBytes(StandardCharsets.UTF_8));

            // Base64编码
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1算法不可用", e);
        }
    }

    public static void main(String[] args) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        String clientKey = "LUuzZfr3EcBkWUKjvvFbZA==";
        //String clientKey = new String(Base64.getEncoder().encode(bytes));
        String acceptKey = generateWebSocketAccept(clientKey);

        System.out.println("客户端密钥: " + clientKey);
        System.out.println("服务器Accept: " + acceptKey);
    }
}
