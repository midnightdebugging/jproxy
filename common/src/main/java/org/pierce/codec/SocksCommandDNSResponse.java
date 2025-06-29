package org.pierce.codec;

import java.util.ArrayList;
import java.util.List;

public class SocksCommandDNSResponse implements SocksCommand {

    SocksCommandResponseCode code;

    String domain;

    boolean keep = true;



    List<String> ipList = new ArrayList<>();

    public SocksCommandResponseCode getCode() {
        return code;
    }

    public void setCode(SocksCommandResponseCode code) {
        this.code = code;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<String> getIpList() {
        return ipList;
    }

    public void setIpList(List<String> ipList) {
        this.ipList = ipList;
    }

    public void addIp(String ip) {
        ipList.add(ip);
    }

    @Override
    public boolean isKeep() {
        return keep;
    }
    public void setKeep(boolean keep) {
        this.keep = keep;
    }
}
