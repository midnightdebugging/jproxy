package org.pierce;

import org.pierce.pki.ECCPKIInstaller;
import org.pierce.pki.PKIInstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Jproxy {
    private static final Jproxy instance = new Jproxy();

    private static final Logger log = LoggerFactory.getLogger(Jproxy.class);

    private Jproxy() {


    }

    public static Jproxy getInstance() {
        return instance;
    }

    public void initialize() {
        String path = JproxyProperties.getProperty("jproxy.config-path");
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
