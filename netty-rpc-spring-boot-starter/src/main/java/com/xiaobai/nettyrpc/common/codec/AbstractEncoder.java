package com.xiaobai.nettyrpc.common.codec;

import com.xiaobai.nettyrpc.common.dto.TransferDTO;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

/**
 * 抽象encoder
 *
 * @author yinzhaojing
 * @date 2022-06-21 17:38:31
 */
public abstract class AbstractEncoder extends MessageToByteEncoder<TransferDTO> {

    @Override
    public void encode(ChannelHandlerContext ctx, TransferDTO msg, ByteBuf out) throws Exception {
        byte[] bytes = encode(msg);
        out.writeBytes(bytes);
        //根据\r\n进行消息分隔
        out.writeBytes("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    public abstract byte[] encode(TransferDTO msg) throws Exception;
}
