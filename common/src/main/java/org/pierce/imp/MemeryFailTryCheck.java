package org.pierce.imp;

import org.pierce.FailTryCheck;
import org.pierce.UtilTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class MemeryFailTryCheck implements FailTryCheck {

    int MAX_TRY = 2;

    private final HashMap<String, Integer> blackList0 = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(MemeryFailTryCheck.class);

    public MemeryFailTryCheck() {
    }

    @Override
    public synchronized void failCount(String input) {
        log.debug("{}", UtilTools.objToString(blackList0));
        if (!blackList0.containsKey(input)) {
            blackList0.put(input, 1);
            return;
        }
        blackList0.put(input, blackList0.get(input) + 1);
    }

    @Override
    public synchronized boolean check(String input) {
        log.debug("{}", UtilTools.objToString(blackList0));
        if (blackList0.containsKey(input)) {
            log.debug("blackList0.get(input):{}", blackList0.get(input));
            if (blackList0.get(input) >= MAX_TRY) {
                log.info("0--blackList0.get(input) >= {}:{}", blackList0.get(input) >= MAX_TRY, MAX_TRY);
                return false;
            }
            log.debug("1--blackList0.get(input) >= {}:{}", blackList0.get(input) >= MAX_TRY, MAX_TRY);

        }
        return true;
    }
}
