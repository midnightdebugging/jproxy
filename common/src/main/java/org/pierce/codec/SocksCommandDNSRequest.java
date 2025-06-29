package org.pierce.codec;

public class SocksCommandDNSRequest implements SocksCommand {

    String domain;

    boolean keep = true;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
    @Override
    public boolean isKeep() {
        return keep;
    }
    public void setKeep(boolean keep) {
        this.keep = keep;
    }
}
