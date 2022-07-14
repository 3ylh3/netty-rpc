package com.xiaobai.nettyrpc.consumer.processor;

import com.alibaba.fastjson.JSONObject;
import com.xiaobai.nettyrpc.common.dto.TransferDTO;

/**
 * 消费者前置处理器
 *
 * @author yinzhaojing
 * @date 2022-07-06 19:03:38
 */
public interface ConsumerPreProcessor {
    /**
     * 前置处理
     * @param requestDTO 请求DTO
     * @param params 参数
     * @throws Exception 异常
     */
    void doPreProcess(TransferDTO requestDTO, JSONObject params) throws Exception;
}
