package com.xiaobai.nettyrpc.codec;

import com.xiaobai.nettyrpc.dto.TransferDTO;
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

    public abstract TransferDTO decode(byte[] bytes) throws Exception;
}
