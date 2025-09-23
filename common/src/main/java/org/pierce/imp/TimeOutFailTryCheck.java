package org.pierce.imp;

import org.pierce.FailTryCheck;

public class TimeOutFailTryCheck implements FailTryCheck {

    /**
     * 值班FailTryCheck，是一个数组索引
     */
    int index;

    /**
     * 职员信息表
     */
    FailTryCheck[] workerArr;

    /**
     *
     **/
    long startTime;
    /**
     * 最快10分钟过期，最慢20分钟过期
     */
    final static long MAX_TIME = 1000 * 60 * 10;

    public TimeOutFailTryCheck(FailTryCheck f0, FailTryCheck f1) {
        this.workerArr = new FailTryCheck[]{f0, f1};
        startTime = System.currentTimeMillis();
    }

    @Override
    public void failCount(String input) {
        dutyCheck();
        workerArr[index].failCount(input);
    }

    @Override
    public boolean check(String input) {
        return workerArr[0].check(input) && workerArr[1].check(input);
    }

    public synchronized void dutyCheck() {
        if (System.currentTimeMillis() - startTime > MAX_TIME) {
            startTime = System.currentTimeMillis();
            //先清理状态记录，然后切岗
            workerArr[index ^ 1].clear();
            index = index ^ 1;
        }
    }

    @Override
    public void clear() {
        workerArr[0].clear();
        workerArr[1].clear();
    }
}
