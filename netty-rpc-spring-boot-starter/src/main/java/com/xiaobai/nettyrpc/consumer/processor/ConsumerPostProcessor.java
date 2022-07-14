package com.xiaobai.nettyrpc.consumer.processor;

import com.alibaba.fastjson.JSONObject;
import com.xiaobai.nettyrpc.common.dto.TransferDTO;

/**
 * 消费者后置处理
 *
 * @author yinzhaojing
 * @date 2022-07-06 19:08:12
 */
public interface ConsumerPostProcessor {
    /**
     * 后置处理
     * @param responseDTO 返回DTO
     * @param params 参数
     * @throws Exception 异常
     */
    void doPostProcess(TransferDTO responseDTO, JSONObject params) throws Exception;
}
