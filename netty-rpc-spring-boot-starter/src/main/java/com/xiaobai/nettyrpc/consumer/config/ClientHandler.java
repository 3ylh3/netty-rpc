package com.xiaobai.nettyrpc.consumer.config;

import com.xiaobai.nettyrpc.dto.TransferDTO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * client handler
 *
 * @author yinzhaojing
 * @date 2022-06-20 20:05:08
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    /**
     * 接收并缓存远程服务端返回消息
     * @param ctx ChannelHandlerContext
     * @param msg 返回TransferDTO
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        TransferDTO response = (TransferDTO) msg;
        response.setRemoteAddress(ctx.channel().remoteAddress().toString());
        ResponseCache.put(response.getRequestId(), response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("remote service {} throws exception:", ctx.channel().remoteAddress(), cause);
        ctx.close();
    }
}
