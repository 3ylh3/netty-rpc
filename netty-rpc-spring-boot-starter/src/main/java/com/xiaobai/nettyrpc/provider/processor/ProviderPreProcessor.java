package com.xiaobai.nettyrpc.provider.processor;

import com.xiaobai.nettyrpc.common.dto.TransferDTO;

/**
 * 提供者前置处理
 *
 * @author yinzhaojing
 * @date 2022-07-06 20:12:41
 */
public interface ProviderPreProcessor {
    /**
     * 前置处理
     * @param requestDTO 请求DTO
     * @throws Exception 异常
     */
    void doPreProcess(TransferDTO requestDTO) throws Exception;
}
