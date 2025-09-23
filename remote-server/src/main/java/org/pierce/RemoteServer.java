package org.pierce;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import org.pierce.nlist.NameListCheck;
import org.pierce.nlist.imp.TextNameListCheck;
import org.pierce.socks.RemoteSocks;
import org.pierce.websocket.RemoteWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RemoteServer {

    private static final Logger log = LoggerFactory.getLogger(RemoteServer.class);

    private final static NameListCheck nameListCheck = new TextNameListCheck() {
        {
            super.loadByInputStream();
        }
    };

    public static NameListCheck getNameListCheck() {
        return nameListCheck;
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
        JproxyServer remoteSocks = new RemoteSocks();
        remoteSocks.start(eventLoopGroup);

        JproxyServer remoteWebSocket = new RemoteWebSocket();
        remoteWebSocket.start(eventLoopGroup);

    }
}
