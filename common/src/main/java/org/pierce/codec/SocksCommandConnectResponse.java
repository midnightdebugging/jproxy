package org.pierce.codec;

public class SocksCommandConnectResponse implements SocksCommand {

    SocksCommandResponseCode code;

    boolean keep = true;

    public SocksCommandResponseCode getCode() {
        return code;
    }

    public void setCode(SocksCommandResponseCode code) {
        this.code = code;
    }
    @Override
    public boolean isKeep() {
        return keep;
    }
    public void setKeep(boolean keep) {
        this.keep = keep;
    }

}
