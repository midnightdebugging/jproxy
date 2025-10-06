package org.pierce;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeTask {
    public static void main(String[] args) {

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                scheduler.close();
                System.out.println("scheduler.close()");
            }
        });
        scheduler.scheduleAtFixedRate(() -> {
                    // 执行任务的代码
                    System.out.println("任务执行: " + new Date());
                },
                0,      // 初始延迟（立即开始）
                1,     // 执行间隔
                TimeUnit.SECONDS
        );


    }
}
