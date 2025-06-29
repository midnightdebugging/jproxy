package org.pierce.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.handler.ssl.*;
import org.pierce.JproxyProperties;

import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TlsClientHandlerBuilder {

    private static final TlsClientHandlerBuilder instance = new TlsClientHandlerBuilder();

    private SslContext sslContext = null;

    public static TlsClientHandlerBuilder getInstance() {
        return instance;
    }

    public ChannelHandler build(Channel ch) {
        if (sslContext == null) {
            sslContext = createClientSslContext();
        }
        // 创建 SSL 引擎并配置
        //sslEngine = clientSslContext.newEngine(ch.alloc());
        SSLEngine sslEngine = sslContext.newEngine(ch.alloc(), JproxyProperties.getProperty("server-name"), 8443);
        sslEngine.setUseClientMode(true); // 客户端模式
        return new SslHandler(sslEngine);
    }

    public SslContext createClientSslContext() {
        //1. 加载信任的根证书（CA）
        try (InputStream cerIs = Files.newInputStream(Paths.get(JproxyProperties.getProperty("tls.client-cert.path")));
             InputStream keyIs = Files.newInputStream(Paths.get(JproxyProperties.getProperty("tls.client-key.path")));
             InputStream ca = Files.newInputStream(Paths.get(JproxyProperties.getProperty("tls.ca-cert.path")))) {
            // 2. 构建 SslContext
            return SslContextBuilder.forClient()
                    .keyManager(cerIs, keyIs)
                    .trustManager(ca)
                    .clientAuth(ClientAuth.REQUIRE)
                    .sslProvider(SslProvider.OPENSSL) // 使用OpenSSL提供更高性能

                    //.ciphers(CipherSuiteFilterFactory.getSecureCiphers()) // 指定安全密码套件
                    .protocols("TLSv1.3") // 强制使用高版本协议
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
