package org.pierce.entity;

public class ProtocolInfo {
    String protocol;

    String hostAddress;

    int port;

    String path;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
