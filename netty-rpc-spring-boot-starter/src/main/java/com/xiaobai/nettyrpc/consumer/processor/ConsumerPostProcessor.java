package com.xiaobai.nettyrpc.consumer.processor;

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
     * @throws Exception 异常
     */
    void doPostProcess(TransferDTO responseDTO) throws Exception;
}
