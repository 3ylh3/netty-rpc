package com.xiaobai.nettyrpc.common.codec;

import com.xiaobai.nettyrpc.common.dto.TransferDTO;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.xerial.snappy.Snappy;

import java.util.List;

/**
 * 抽象decoder
 *
 * @author yinzhaojing
 * @date 2022-06-21 17:46:16
 */
public abstract class AbstractDecoder extends ByteToMessageDecoder {

    /**
     * 是否压缩
     */
    private boolean isCompression = false;

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (0 >= in.readableBytes()) {
            return;
        }
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        TransferDTO transferDTO = new TransferDTO();
        if (!isCompression) {
            // 未开启数据压缩
            transferDTO = decode(bytes);
        } else {
            // 开启数据压缩
            transferDTO = decode(Snappy.uncompress(bytes));
        }
        out.add(transferDTO);
        in.skipBytes(in.readableBytes());
    }

    public void setCompression(Boolean isCompression) {
        this.isCompression = isCompression;
    }

    /**
     * 解码
     * @param bytes byte数组
     * @return 解码后返回对象
     * @throws Exception 异常
     */
    public abstract TransferDTO decode(byte[] bytes) throws Exception;
}
