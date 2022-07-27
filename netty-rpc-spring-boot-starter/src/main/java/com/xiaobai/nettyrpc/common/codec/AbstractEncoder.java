package com.xiaobai.nettyrpc.common.codec;

import com.xiaobai.nettyrpc.common.dto.TransferDTO;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.xerial.snappy.Snappy;

import java.nio.charset.StandardCharsets;

/**
 * 抽象encoder
 *
 * @author yinzhaojing
 * @date 2022-06-21 17:38:31
 */
public abstract class AbstractEncoder extends MessageToByteEncoder<TransferDTO> {

    /**
     * 是否压缩
     */
    private boolean isCompression = false;

    @Override
    public void encode(ChannelHandlerContext ctx, TransferDTO msg, ByteBuf out) throws Exception {
        byte[] bytes = encode(msg);
        if (!isCompression) {
            // 不开启数据压缩
            out.writeBytes(bytes);
        } else {
            // 开启数据压缩
            out.writeBytes(Snappy.compress(bytes));
        }
        //根据\r\n进行消息分隔
        out.writeBytes("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    public void setCompression(Boolean isCompression) {
        this.isCompression = isCompression;
    }

    /**
     * 编码
     * @param msg 请求信息
     * @return 编码后byte数组
     * @throws Exception 异常
     */
    public abstract byte[] encode(TransferDTO msg) throws Exception;
}
