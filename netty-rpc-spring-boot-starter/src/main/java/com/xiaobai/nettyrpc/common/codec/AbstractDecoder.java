package com.xiaobai.nettyrpc.common.codec;

import com.xiaobai.nettyrpc.common.dto.TransferDTO;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 抽象decoder
 *
 * @author yinzhaojing
 * @date 2022-06-21 17:46:16
 */
public abstract class AbstractDecoder extends ByteToMessageDecoder {

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (0 >= in.readableBytes()) {
            return;
        }
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        TransferDTO transferDTO = decode(bytes);
        out.add(transferDTO);
        in.skipBytes(in.readableBytes());
    }

    /**
     * 解码
     * @param bytes byte数组
     * @return 解码后返回对象
     * @throws Exception 异常
     */
    public abstract TransferDTO decode(byte[] bytes) throws Exception;
}
