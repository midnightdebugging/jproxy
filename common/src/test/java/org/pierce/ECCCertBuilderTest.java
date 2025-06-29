package org.pierce;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.pierce.imp.DefaultSelector;
import org.pierce.pki.ECCPKIInstaller;

import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.Security;

public class ECCCertBuilderTest {

    public static void main(String[] args) throws Exception {

        String keyWord0 = new DefaultSelector<String>().select(ECCPKIInstaller.NOUNS);
        String keyWord1 = new DefaultSelector<String>().select(ECCPKIInstaller.TLDS);

        keyWord0 = keyWord0.toLowerCase();
        ECCPKIInstaller ECCPKIBuild = new ECCPKIInstaller();

        Security.addProvider(new BouncyCastleProvider());

        // 1. 生成ECC密钥对
        KeyPair caKeyPair = ECCPKIBuild.genKeyPair();

        try (FileWriter fileWriter = new FileWriter(JproxyProperties.getProperty("tls.ca-key.path")); JcaPEMWriter jcaPEMWriter = new JcaPEMWriter(fileWriter)) {
            jcaPEMWriter.writeObject(caKeyPair);
            jcaPEMWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 2. 生成自签名证书
        X509CertificateHolder caCert = ECCPKIBuild.selfSign(String.format("CN=%s%s", keyWord0, keyWord1), caKeyPair);


        try (FileWriter fileWriter = new FileWriter(JproxyProperties.getProperty("tls.ca-cert.path")); JcaPEMWriter jcaPEMWriter = new JcaPEMWriter(fileWriter)) {
            jcaPEMWriter.writeObject(caCert);
            jcaPEMWriter.flush();
        }

        KeyPair server01KeyPair = ECCPKIBuild.genKeyPair();

        try (FileWriter fileWriter = new FileWriter(JproxyProperties.getProperty("tls.server-key.path")); JcaPEMWriter jcaPEMWriter = new JcaPEMWriter(fileWriter)) {
            jcaPEMWriter.writeObject(server01KeyPair);
            jcaPEMWriter.flush();
        }
        X509CertificateHolder server01Cert = ECCPKIBuild.sign(caCert, "CN=server." + keyWord0 + keyWord1, server01KeyPair, caKeyPair, "server." + keyWord0 + keyWord1);

        try (FileWriter fileWriter = new FileWriter(JproxyProperties.getProperty("tls.server-cert.path")); JcaPEMWriter jcaPEMWriter = new JcaPEMWriter(fileWriter)) {
            jcaPEMWriter.writeObject(server01Cert);
            jcaPEMWriter.flush();
        }


        KeyPair client01KeyPair = ECCPKIBuild.genKeyPair();

        try (FileWriter fileWriter = new FileWriter(JproxyProperties.getProperty("tls.client-key.path")); JcaPEMWriter jcaPEMWriter = new JcaPEMWriter(fileWriter)) {
            jcaPEMWriter.writeObject(client01KeyPair);
            jcaPEMWriter.flush();
        }
        X509CertificateHolder client01Cert = ECCPKIBuild.sign(caCert, "CN=client." + keyWord0 + keyWord1, client01KeyPair, caKeyPair, "client." + keyWord0 + keyWord1);

        try (FileWriter fileWriter = new FileWriter(JproxyProperties.getProperty("tls.client-cert.path")); JcaPEMWriter jcaPEMWriter = new JcaPEMWriter(fileWriter)) {
            jcaPEMWriter.writeObject(client01Cert);
            jcaPEMWriter.flush();
        }

        try (FileWriter fileWriter = new FileWriter(JproxyProperties.getProperty("tls.properties"))) {
            fileWriter.write("server-name=server." + keyWord0 + keyWord1);
            fileWriter.flush();
        }

    }
}
