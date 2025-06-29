package org.pierce.codec;

public class SocksCommandClose implements SocksCommand {

    boolean keep = true;

    @Override
    public boolean isKeep() {
        return keep;
    }
}
