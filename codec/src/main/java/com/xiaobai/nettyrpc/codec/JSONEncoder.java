package com.xiaobai.nettyrpc.codec;

import com.alibaba.fastjson.JSONObject;
import com.xiaobai.nettyrpc.dto.TransferDTO;

import java.nio.charset.StandardCharsets;

/**
 * JSON encoder
 *
 * @author yinzhaojing
 * @date 2022-06-21 16:21:34
 */
public class JSONEncoder extends AbstractEncoder {

    @Override
    public byte[] encode(TransferDTO msg) {
        String message = JSONObject.toJSONString(msg);
        return message.getBytes(StandardCharsets.UTF_8);
    }
}
