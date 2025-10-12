package org.pierce;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import org.pierce.pki.ECCPKIInstaller;
import org.pierce.pki.PKIInstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Jproxy {

    private static final Jproxy instance = new Jproxy();

    private static final Logger log = LoggerFactory.getLogger(Jproxy.class);

    static EventLoopGroup eventLoopGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

    static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    List<Runnable> runnableList = new LinkedList<>();

    static {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    log.info("eventLoopGroup.shutdownGracefully();");
                    eventLoopGroup.shutdownGracefully();
                } catch (Exception e) {
                    log.info("eventLoopGroup.shutdownGracefully();", e);
                }

                try {
                    log.info("scheduler.close();");
                    scheduler.close();
                } catch (Exception e) {
                    log.info("scheduler.close();", e);
                }

            }
        });
    }

    public static EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    private Jproxy() {


    }

    public void addRunnable(Runnable runnable) {
        runnableList.add(runnable);
    }

    public static Jproxy getInstance() {
        return instance;
    }

    public void initialize(Class<?> programClazz, boolean useDatabase) {

        // 获取当前进程ID
        long pid = ProcessHandle.current().pid();

        // 指定输出文件路径
        Path filePath = Path.of(String.format("%s.pid", programClazz.getName()));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            boolean delete = filePath.toFile().delete();
            log.info("删除文件PID文件: {}", delete);
        }));

        try {
            // 将PID写入文件（覆盖原有内容）
            Files.writeString(
                    filePath,
                    String.valueOf(pid),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
            log.info("PID已写入文件: {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            log.info("写入文件失败: {}", e.getMessage());
        }

        String path = JproxyProperties.getProperty("jproxy.config-path");

        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                log.error("file.mkdirs(),error : {}", path);
                System.exit(-1);
                return;
            }
        }
        if (useDatabase) {
            try {
                DataBase.initialize("/sql/create_HostName2Address.sql", "/sql/create_NameList.sql", "/sql/insert_NameList.sql");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        File tlsFile = new File(JproxyProperties.getProperty("tls.properties"));
        if (!tlsFile.exists()) {
            PKIInstaller pkiInstaller = new ECCPKIInstaller();
            try {
                pkiInstaller.install();
                JproxyProperties.reloadTlsProperties();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (JproxyProperties.booleanVal("debug")) {
            scheduler.scheduleAtFixedRate(() -> {
                        for (Runnable runnable : runnableList) {
                            try {
                                runnable.run();
                            } catch (Throwable t) {
                                log.info("Throwable", t);
                            }

                        }
                    },
                    0,      // 初始延迟（立即开始）
                    10,     // 执行间隔
                    TimeUnit.SECONDS
            );
        }
    }
}
