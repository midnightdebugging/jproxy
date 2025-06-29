package org.pierce.codec;

public class SocksCommandConnectRequest implements SocksCommand {
    String target;

    int port;

    boolean keep = true;


    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean isKeep() {
        return keep;
    }
    public void setKeep(boolean keep) {
        this.keep = keep;
    }
}
