package com.xiaobai.nettyrpc.consumer.config;

import com.xiaobai.nettyrpc.dto.TransferDTO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 请求id对应返回信息缓存
 *
 * @author yinzhaojing
 * @date 2022-06-21 11:06:51
 */
public class ResponseCache {
    private static final Map<String, TransferDTO> CACHE = new ConcurrentHashMap<>(8);

    public static void put(String requestId, TransferDTO response) {
        CACHE.put(requestId, response);
    }

    /**
     * 获取返回消息
     * @param requestId 请求id
     * @return 返回消息
     */
    public static TransferDTO get(String requestId) {
        if (CACHE.containsKey(requestId)) {
            return CACHE.remove(requestId);
        } else {
            // 未收到返回消息
            return null;
        }
    }
}
