package com.xiaobai.nettyrpc.provider.processor;

import com.xiaobai.nettyrpc.dto.TransferDTO;

/**
 * 提供者后置处理
 *
 * @author yinzhaojing
 * @date 2022-07-06 20:15:50
 */
public interface ProviderPostProcessor {
    /**
     * 后置处理
     * @param responseDTO 返回DTO
     * @throws Exception 异常
     */
    void doPostProcess(TransferDTO responseDTO) throws Exception;
}
