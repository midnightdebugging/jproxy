package org.pierce;

import org.pierce.pki.ECCPKIInstaller;
import org.pierce.pki.PKIInstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Jproxy {
    private static final Jproxy instance = new Jproxy();

    private static final Logger log = LoggerFactory.getLogger(Jproxy.class);

    private Jproxy() {


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
        if (useDatabase) {
            File file = new File(path);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    log.error("file.mkdirs(),error : {}", path);
                    System.exit(-1);
                    return;
                }
            }
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
    }
}
