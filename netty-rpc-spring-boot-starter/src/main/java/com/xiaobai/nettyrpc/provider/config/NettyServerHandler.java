package com.xiaobai.nettyrpc.provider.config;

import com.xiaobai.nettyrpc.common.constants.CommonConstants;
import com.xiaobai.nettyrpc.common.exceptions.RemoteCallException;
import com.xiaobai.nettyrpc.dto.TransferDTO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * netty server handler
 *
 * @author yinzhaojing
 * @date 2022-06-20 19:49:06
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        TransferDTO requestDTO = (TransferDTO) msg;
        TransferDTO responseDTO = new TransferDTO();
        responseDTO.copyRequestValue(requestDTO);
        try {
            // 反射获取调用接口的实现类
            String interfaceName = requestDTO.getInterfaceName();
            String methodName = requestDTO.getMethodName();
            Class<?>[] parameterTypes = requestDTO.getParameterTypes();
            Object[] params = requestDTO.getParams();
            logger.info("accept remote call,consumer name:{},interface:{},method:{},request id:{}",
                    requestDTO.getConsumerName(), interfaceName, methodName, requestDTO.getRequestId());
            ProviderService providerService = ProviderServiceCache.get(interfaceName);
            if (null == providerService) {
                throw new RemoteCallException("no impl fine,interface:" + interfaceName);
            }
            Class<?> clazz = Class.forName(providerService.getImplName());
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            Object result = method.invoke(clazz.newInstance(), params);
            responseDTO.setResponseCode(CommonConstants.SUCCESS_CODE);
            responseDTO.setResponseMessage(CommonConstants.SUCCESS_MESSAGE);
            responseDTO.setProviderName(providerService.getProviderName());
            responseDTO.setResult(result);
            logger.info("remote call success,client address:{}", ctx.channel().remoteAddress());
        } catch (Exception e) {
            logger.error("remote call exception:", e);
            responseDTO.setResponseCode(CommonConstants.ERROR_CODE);
            responseDTO.setResponseMessage(e.getMessage());
        }
        ctx.channel().write(responseDTO);
        // 使用\r\n分隔消息
        ctx.channel().writeAndFlush(CommonConstants.LINE_DELIMITER);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("exception:", cause);
        ctx.close();
    }
}
