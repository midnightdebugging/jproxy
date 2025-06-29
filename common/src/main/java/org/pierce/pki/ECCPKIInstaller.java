package org.pierce.pki;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.pierce.JproxyProperties;
import org.pierce.imp.DefaultSelector;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.Date;

public class ECCPKIInstaller implements PKIInstaller {

    public final static String[] NOUNS = {
            // 动物名称数组 (40个)
            "Lion", "Tiger", "Elephant", "Giraffe", "Zebra",
            "Kangaroo", "Koala", "Panda", "Hippopotamus", "Rhino",
            "Crocodile", "Gorilla", "Chimpanzee", "Leopard", "Cheetah",
            "Wolf", "Fox", "Bear", "Polar Bear", "Penguin",
            "Dolphin", "Shark", "Whale", "Octopus", "Eagle",
            "Owl", "Parrot", "Peacock", "Swan", "Flamingo",
            "Butterfly", "Bee", "Ant", "Spider", "Scorpion",
            "Horse", "Cow", "Sheep", "Goat", "Rabbit",
            // 植物名称数组 (35个)
            "Oak", "Pine", "Maple", "Willow", "Palm",
            "Bamboo", "Cactus", "Fern", "Moss", "Ivy",
            "Rose", "Tulip", "Sunflower", "Orchid", "Daisy",
            "Lily", "Lavender", "Jasmine", "Carnation", "Daffodil",
            "Cedar", "Sequoia", "Redwood", "Birch", "Cherry Blossom",
            "Baobab", "Bonsai", "Cypress", "Eucalyptus", "Ficus",
            "Magnolia", "Olive", "Sakura", "Spruce", "Wisteria",
            // 水果名称数组 (35个)
            "Apple", "Banana", "Orange", "Grape", "Strawberry",
            "Watermelon", "Pineapple", "Mango", "Kiwi", "Peach",
            "Pear", "Cherry", "Plum", "Lemon", "Lime",
            "Coconut", "Papaya", "Avocado", "Pomegranate", "Fig",
            "Blueberry", "Raspberry", "Blackberry", "Cranberry", "Apricot",
            "Guava", "Passion Fruit", "Dragon Fruit", "Lychee", "Melon",
            "Cantaloupe", "Tangerine", "Grapefruit", "Kumquat", "Persimmon"
    };

    // 顶级域名数组 (220个)
    public final static String[] TLDS = {
            // 通用顶级域名 (gTLD)
            ".com", ".org", ".net", ".edu", ".gov", ".mil", ".int",
            ".biz", ".info", ".name", ".pro", ".aero", ".cat", ".coop",
            ".jobs", ".mobi", ".museum", ".travel", ".asia",

            // 国家代码顶级域名 (ccTLD)
            /*".ac", ".ad", ".ae", ".af", ".ag", ".ai", ".al", ".am",
            ".an", ".ao", ".aq", ".ar", ".as", ".at", ".au", ".aw",
            ".ax", ".az", ".ba", ".bb", ".bd", ".be", ".bf", ".bg",
            ".bh", ".bi", ".bj", ".bm", ".bn", ".bo", ".br", ".bs",
            ".bt", ".bv", ".bw", ".by", ".bz", ".ca", ".cc", ".cd",
            ".cf", ".cg", ".ch", ".ci", ".ck", ".cl", ".cm", ".cn",
            ".co", ".cr", ".cu", ".cv", ".cw", ".cx", ".cy", ".cz",
            ".de", ".dj", ".dk", ".dm", ".do", ".dz", ".ec", ".ee",
            ".eg", ".eh", ".er", ".es", ".et", ".eu", ".fi", ".fj",
            ".fk", ".fm", ".fo", ".fr", ".ga", ".gb", ".gd", ".ge",
            ".gf", ".gg", ".gh", ".gi", ".gl", ".gm", ".gn", ".gp",
            ".gq", ".gr", ".gs", ".gt", ".gu", ".gw", ".gy", ".hk",
            ".hm", ".hn", ".hr", ".ht", ".hu", ".id", ".ie", ".il",
            ".im", ".in", ".io", ".iq", ".ir", ".is", ".it", ".je",
            ".jm", ".jo", ".jp", ".ke", ".kg", ".kh", ".ki", ".km",
            ".kn", ".kp", ".kr", ".kw", ".ky", ".kz", ".la", ".lb",
            ".lc", ".li", ".lk", ".lr", ".ls", ".lt", ".lu", ".lv",
            ".ly", ".ma", ".mc", ".md", ".me", ".mg", ".mh", ".mk",
            ".ml", ".mm", ".mn", ".mo", ".mp", ".mq", ".mr", ".ms",
            ".mt", ".mu", ".mv", ".mw", ".mx", ".my", ".mz", ".na",
            ".nc", ".ne", ".nf", ".ng", ".ni", ".nl", ".no", ".np",
            ".nr", ".nu", ".nz", ".om", ".pa", ".pe", ".pf", ".pg",
            ".ph", ".pk", ".pl", ".pm", ".pn", ".pr", ".ps", ".pt",
            ".pw", ".py", ".qa", ".re", ".ro", ".rs", ".ru", ".rw",
            ".sa", ".sb", ".sc", ".sd", ".se", ".sg", ".sh", ".si",
            ".sj", ".sk", ".sl", ".sm", ".sn", ".so", ".sr", ".ss",
            ".st", ".su", ".sv", ".sx", ".sy", ".sz", ".tc", ".td",
            ".tf", ".tg", ".th", ".tj", ".tk", ".tl", ".tm", ".tn",
            ".to", ".tr", ".tt", ".tv", ".tw", ".tz", ".ua", ".ug",
            ".uk", ".us", ".uy", ".uz", ".va", ".vc", ".ve", ".vg",
            ".vi", ".vn", ".vu", ".wf", ".ws", ".ye", ".yt", ".za",
            ".zm", ".zw",*/

            // 新顶级域名 (nTLD)
            ".xyz", ".online", ".site", ".tech", ".space", ".store",
            ".shop", ".club", ".vip", ".app", ".dev", ".blog", ".cloud",
            ".design", ".fun", ".games", ".live", ".link", ".network",
            ".news", ".one", ".studio", ".top", ".win", ".work", ".world"
    };
    private static final long effectiveTime = 365 * 24 * 60 * 60 * 1000L;

    // 也可选 "secp384r1", "secp521r1"

    private static final String CURVE_NAME = "secp256r1";

    public KeyPair genKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec(CURVE_NAME);
        keyGen.initialize(ecSpec, new SecureRandom());
        return keyGen.generateKeyPair();
    }

    public X509CertificateHolder selfSign(String distinguishedName, KeyPair certKeyPair) throws CertIOException, OperatorCreationException {
        // 设置证书有效期（1年）
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + effectiveTime);

        // 创建颁发者/使用者名称（这里相同因为是自签名）
        X500Name subject = new X500Name(distinguishedName);

        // 创建证书构建器
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                subject,
                serial,
                startDate,
                endDate,
                subject,
                certKeyPair.getPublic()
        );

        certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true)); // CA标识
        certBuilder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));

        // 创建签名者
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withECDSA")
                .setProvider("BC")
                .build(certKeyPair.getPrivate());

        // 生成证书
        return certBuilder.build(signer);
    }

    public X509CertificateHolder sign(X509CertificateHolder caCert, String distinguishedName, KeyPair certKeyPair, KeyPair signKeyPair, String sanName) throws Exception {
        // 设置证书有效期（1年）
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + effectiveTime);
        X500Name subject = new X500Name(distinguishedName);

        // 创建证书构建器
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                caCert.getIssuer(),
                serial,
                startDate,
                endDate,
                subject,
                certKeyPair.getPublic()
        );

        // 添加扩展
        certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false)); // 非CA证书
        certBuilder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
        certBuilder.addExtension(Extension.authorityKeyIdentifier, false, new JcaX509ExtensionUtils()
                .createAuthorityKeyIdentifier(caCert));

        if (sanName != null) {
            san(certBuilder, "DNS:" + sanName, "DNS:*." + sanName);
        }

        // 创建签名者
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withECDSA")
                .setProvider("BC")
                .build(signKeyPair.getPrivate());

        // 生成证书
        return certBuilder.build(signer);
    }

    public static void san(X509v3CertificateBuilder certBuilder, String... names)
            throws Exception {

        GeneralName[] generalNames = new GeneralName[names.length];

        for (int i = 0; i < names.length; i++) {
            String[] parts = names[i].split(":", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid SAN format: " + names[i]);
            }

            String type = parts[0].toUpperCase();
            String value = parts[1];

            switch (type) {
                case "DNS":
                    generalNames[i] = new GeneralName(GeneralName.dNSName, value);
                    break;
                case "IP":
                    generalNames[i] = new GeneralName(GeneralName.iPAddress, value);
                    break;
                case "EMAIL":
                case "MAIL":
                    generalNames[i] = new GeneralName(GeneralName.rfc822Name, value);
                    break;
                case "URI":
                    generalNames[i] = new GeneralName(GeneralName.uniformResourceIdentifier, value);
                    break;
                case "DIRNAME":
                    generalNames[i] = new GeneralName(GeneralName.directoryName, new X500Name(value));
                    break;
                case "OID":
                    generalNames[i] = new GeneralName(GeneralName.registeredID, new ASN1ObjectIdentifier(value));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported SAN type: " + type);
            }
        }

        // 创建SAN扩展
        GeneralNames subjectAltNames = new GeneralNames(generalNames);
        certBuilder.addExtension(Extension.subjectAlternativeName, false, subjectAltNames);
    }

    public void install() throws Exception {

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
