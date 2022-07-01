package com.xiaobai.nettyrpc.consumer.config;

import com.xiaobai.nettyrpc.common.constants.CommonConstants;
import com.xiaobai.nettyrpc.dto.TransferDTO;
import io.netty.channel.ChannelFuture;

/**
 * netty客户端
 *
 * @author yinzhaojing
 * @date 2022-06-23 11:25:20
 */
public class NettyClient {
    /**
     * 超时时间，单位秒
     */
    private Integer timeout;
    /**
     * channel
     */
    private ChannelFuture channelFuture;

    public NettyClient(Integer timeout, ChannelFuture channelFuture) {
        this.timeout = timeout;
        this.channelFuture = channelFuture;
    }

    /**
     * 发送消息
     * @param requestDTO 请求DTO
     * @return 返回DTO
     */
    public TransferDTO send(TransferDTO requestDTO) {
        channelFuture.channel().writeAndFlush(requestDTO);
        long start = System.currentTimeMillis();
        while(true) {
            if (System.currentTimeMillis() - start > timeout * 1000) {
                // 超时
                TransferDTO response = new TransferDTO();
                response.setResponseCode(CommonConstants.TIMEOUT_CODE);
                response.setResponseMessage(CommonConstants.TIMEOUT_MESSAGE);
                response.copyRequestValue(requestDTO);
                return response;
            } else {
                TransferDTO responseDTO = ResponseCache.get(requestDTO.getRequestId());
                if (null != responseDTO) {
                    // 收到返回
                    return responseDTO;
                }
            }
        }
    }

    /**
     * 关闭客户端
     */
    public void close() {
        this.channelFuture.channel().close();
    }
}
