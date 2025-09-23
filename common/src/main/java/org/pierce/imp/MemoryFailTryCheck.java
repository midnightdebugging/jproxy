package org.pierce.imp;

import org.pierce.FailTryCheck;
import org.pierce.UtilTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class MemoryFailTryCheck implements FailTryCheck {

    int MAX_TRY = 5;

    private final HashMap<String, Integer> blackList = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(MemoryFailTryCheck.class);

    public MemoryFailTryCheck() {
    }

    @Override
    public synchronized void failCount(String input) {
        log.info("{}", UtilTools.objToString(blackList));
        if (!blackList.containsKey(input)) {
            blackList.put(input, 1);
            return;
        }
        blackList.put(input, blackList.get(input) + 1);
    }

    @Override
    public synchronized boolean check(String input) {
        log.info("{}", UtilTools.objToString(blackList));
        if (blackList.containsKey(input)) {
            log.info("blackList.get({}) < {}?{}", input, MAX_TRY, blackList.get(input) < MAX_TRY);
            return blackList.get(input) < MAX_TRY;
        }
        return true;
    }

    @Override
    public void clear() {
        blackList.clear();
    }
}
