package org.pierce.imp;

import org.pierce.FailTryCheck;

public class MemoryTimeOutFailTryCheck extends TimeOutFailTryCheck implements FailTryCheck {

    public MemoryTimeOutFailTryCheck() {
        super(new MemoryFailTryCheck(), new MemoryFailTryCheck());
    }
}
