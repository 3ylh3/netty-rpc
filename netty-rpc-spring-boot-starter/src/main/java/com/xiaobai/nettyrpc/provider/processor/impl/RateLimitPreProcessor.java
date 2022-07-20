package com.xiaobai.nettyrpc.provider.processor.impl;

import com.alibaba.fastjson.JSONObject;
import com.xiaobai.nettyrpc.common.constants.CommonConstants;
import com.xiaobai.nettyrpc.common.dto.TransferDTO;
import com.xiaobai.nettyrpc.common.exceptions.RateLimitException;
import com.xiaobai.nettyrpc.common.utils.TimeUtil;
import com.xiaobai.nettyrpc.provider.processor.ProviderPreProcessor;

/**
 * 令牌桶限流
 *
 * @author yinzhaojing
 * @date 2022-07-18 14:59:08
 */
public class RateLimitPreProcessor implements ProviderPreProcessor {
    // 上次更新时间
    private static long lastUpdateTime = TimeUtil.currentTimeMillis();
    // 桶容量,默认500
    private static int capacity = 500;
    // 令牌生成速率（/s），默认200/s
    private static int rate = 200;
    // 当前令牌数
    private static int tokensNumber = 500;
    // 是否初始化
    private static boolean isInit = false;

    /**
     * 前置处理
     * @param requestDTO 请求DTO
     * @param params 参数
     * @throws Exception 异常
     */
    public void doPreProcess(TransferDTO requestDTO, JSONObject params) {
        synchronized (this) {
            if (!isInit) {
                // 初始化
                init(params);
            }
            long currentTime = TimeUtil.currentTimeMillis();
            long timeGap = currentTime - lastUpdateTime;
            // 计算时间段内生成的令牌数
            int tokens = (int) timeGap / 1000 * rate;
            tokensNumber = Math.min(capacity, tokensNumber + tokens);
            if (tokensNumber >= 1) {
                // 有令牌，请求可以通过
                tokensNumber--;
                lastUpdateTime = currentTime;
            } else {
                // 没有令牌，限流
                throw new RateLimitException("request rate limit,request id:" + requestDTO.getRequestId());
            }
        }
    }

    /**
     * 初始化
     * @param params 参数
     */
    private void init(JSONObject params) {
        if (params.containsKey(CommonConstants.RATE_LIMIT_CAPACITY)) {
            capacity = params.getInteger(CommonConstants.RATE_LIMIT_CAPACITY);
            tokensNumber = capacity;
        }
        if (params.containsKey(CommonConstants.RATE_LIMIT_RATE)) {
            rate = params.getInteger(CommonConstants.RATE_LIMIT_RATE);
        }
        isInit = true;
    }
}
