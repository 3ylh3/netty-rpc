package com.xiaobai.nettyrpc.common.codec;

import com.alibaba.fastjson.JSONObject;
import com.xiaobai.nettyrpc.common.dto.TransferDTO;

/**
 * JSON decoder
 *
 * @author yinzhaojing
 * @date 2022-06-21 16:29:19
 */
public class JSONDecoder extends AbstractDecoder {
    @Override
    public TransferDTO decode(byte[] bytes) {
        String message = new String(bytes);
        return JSONObject.parseObject(message, TransferDTO.class);
    }
}
