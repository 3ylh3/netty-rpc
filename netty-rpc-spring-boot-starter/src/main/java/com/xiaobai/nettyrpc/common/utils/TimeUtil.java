package com.xiaobai.nettyrpc.common.utils;

import com.xiaobai.nettyrpc.common.constants.CommonConstants;

import java.util.concurrent.TimeUnit;

/**
 * 时间戳工具类,缓存时间戳，每毫秒更新一次，防止高并发调用System.currentTimeMillis()产生性能问题
 *
 * @author yinzhaojing
 * @date 2022-07-11 10:59:14
 */
public class TimeUtil {
    private static volatile long currentTimeMillis;

    static {
        currentTimeMillis = System.currentTimeMillis();
        Thread daemon = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    currentTimeMillis = System.currentTimeMillis();
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (Throwable e) {

                    }
                }
            }
        });
        daemon.setDaemon(true);
        daemon.setName(CommonConstants.TIME_TICK_THREAD);
        daemon.start();
    }

    public static long currentTimeMillis() {
        return currentTimeMillis;
    }
}
