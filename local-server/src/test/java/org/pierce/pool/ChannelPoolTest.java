package org.pierce.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChannelPoolTest {
    public static void main(String[] args) throws Exception {
        // 启动服务端
        new Thread(() -> {
            try {
                new EchoServer(8888).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // 等待服务端启动
        Thread.sleep(2000);

        // 测试连接池客户端
        FixedChannelPoolClient client = new FixedChannelPoolClient("localhost", 8888);
        client.init();

        // 并发发送消息测试
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 2000; i++) {
            final int index = i;
            executor.submit(() -> {
                client.sendMessage("Message " + index);
            });
        }

        // 等待所有消息发送完成
        Thread.sleep(5000);
        client.shutdown();
        executor.shutdown();
    }
}
