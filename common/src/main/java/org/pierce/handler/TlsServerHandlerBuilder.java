package org.pierce.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;
import org.pierce.JproxyProperties;

import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TlsServerHandlerBuilder {

    private static final TlsServerHandlerBuilder instance = new TlsServerHandlerBuilder();

    private SslContext sslContext = null;

    public static TlsServerHandlerBuilder getInstance() {
        return instance;
    }

    private TlsServerHandlerBuilder() {

    }

    public ChannelHandler build(Channel ch) {
        if (sslContext == null) {
            sslContext = createServerSslContext();
        }
        // 创建 SSL 引擎并配置
        SSLEngine sslEngine = sslContext.newEngine(ch.alloc(), JproxyProperties.getProperty("server-name"), 8443);
        sslContext.newEngine(ch.alloc(), JproxyProperties.getProperty("server-name"), 8443);
        sslEngine.setUseClientMode(false); // 服务端模式
        sslEngine.setNeedClientAuth(true); // 是否启用双向认证（true 表示需要客户端证书）
        return new SslHandler(sslEngine);
    }

    public SslContext createServerSslContext() {

        //1. 加载信任的根证书（CA）
        try (InputStream cerIs = Files.newInputStream(Paths.get(JproxyProperties.getProperty("tls.server-cert.path")));
             InputStream keyIs = Files.newInputStream(Paths.get(JproxyProperties.getProperty("tls.server-key.path")));
             InputStream ca = Files.newInputStream(Paths.get(JproxyProperties.getProperty("tls.ca-cert.path")))) {
            /*CipherSuiteFilterFactory a;*/
            return SslContextBuilder.forServer(cerIs, keyIs)
                    .trustManager(ca)
                    .sslProvider(SslProvider.OPENSSL) // 使用OpenSSL提供更高性能
                    //.ciphers(CipherSuiteFilterFactory.getSecureCiphers()) // 指定安全密码套件
                    // 启用会话缓存并配置参数
                    .sessionCacheSize(1024 * 10) // 最大缓存会话数（默认值：20480）
                    .sessionTimeout(3600)        // 会话超时时间（秒，默认值：300）
                    .protocols("TLSv1.3") // 强制使用高版本协议
                    .build();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
