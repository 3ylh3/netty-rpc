package com.xiaobai.nettyrpc.provider.processor;

import com.alibaba.fastjson.JSONObject;
import com.xiaobai.nettyrpc.common.dto.TransferDTO;

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
     * @param params 参数
     * @throws Exception 异常
     */
    void doPostProcess(TransferDTO responseDTO, JSONObject params) throws Exception;
}
