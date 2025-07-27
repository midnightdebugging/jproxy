package org.pierce;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import org.apache.ibatis.session.SqlSession;
import org.pierce.imp.DefaultSelector;
import org.pierce.impl.DefaultConnectionTypeCheck;
import org.pierce.lhttpproxy.HttpProxyServer;
import org.pierce.lsocks.SocksServer;
import org.pierce.mybatis.entity.HostName2Address;
import org.pierce.mybatis.mapper.HostName2AddressMapper;
import org.pierce.nlist.NameListCheck;
import org.pierce.nlist.imp.DataBaseNameListCheck;
import org.pierce.nlist.imp.FixedReturnConnectListCheck;
import org.pierce.nlist.imp.GFWNameListCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class LocalServer {

    private final static LocalServer instance = new LocalServer();

    private static final Logger log = LoggerFactory.getLogger(LocalServer.class);

    private NameListCheck nameListCheck;

    public ConnectionTypeCheck connectionTypeCheck;


    private final Selector<String> stringSelector = new DefaultSelector<>();

    // private final Selector<HostName2Address> hostName2AddressDefaultSelector = new DefaultSelector<>();

    private LocalServer() {

    }

    public static LocalServer getInstance() {
        return instance;
    }

    public void initialize() {
        Jproxy.getInstance().initialize();
        if ("fixed".equals(JproxyProperties.getProperty("connect-debug"))) {
            nameListCheck = new FixedReturnConnectListCheck();
        } else {
            /*nameListCheck = new DataBaseNameListCheck();*/
            nameListCheck = new GFWNameListCheck() {
                {
                    try {
                        loadConfigure();
                    } catch (IOException e) {
                        log.error("loadConfigure,error", e);
                    }
                }
            };

        }

        connectionTypeCheck = new DefaultConnectionTypeCheck();
    }


    public synchronized void updateHostAddress(String label, String domain, String address) {
        try (SqlSession sqlSession = DataBase.getSqlSessionFactory().openSession()) {
            HostName2AddressMapper mapper = sqlSession.getMapper(HostName2AddressMapper.class);
            HostName2Address hostName2Address = mapper.selectOneByHostNameAndAddress(domain, address);
            if (hostName2Address == null) {
                hostName2Address = new HostName2Address();
                hostName2Address.setHostName(domain);
                hostName2Address.setAddress(address);
                hostName2Address.setLabel(label);
                hostName2Address.setPriority(0);
                hostName2Address.setStatus("00");
                hostName2Address.setUpdateTime(UtilTools.currentTime());
                mapper.insert(hostName2Address);

            }
            sqlSession.commit();
        }
    }

    public NameListCheck getNameListCheck() {
        return nameListCheck;
    }

    public Selector<String> getStringSelector() {
        return stringSelector;
    }

    public static void main(String[] args) {
        EventLoopGroup eventLoopGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                eventLoopGroup.shutdownGracefully();
                log.info("eventLoopGroup.shutdownGracefully()");
            }
        });
        JproxyServer socksServer = new SocksServer();
        socksServer.start(eventLoopGroup);

        JproxyServer httpProxyServer = new HttpProxyServer();
        httpProxyServer.start(eventLoopGroup);

    }
}
