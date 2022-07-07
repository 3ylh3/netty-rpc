package com.xiaobai.nettyrpc.provider.config;

import com.xiaobai.nettyrpc.common.constants.CommonConstants;
import com.xiaobai.nettyrpc.common.exceptions.RemoteCallException;
import com.xiaobai.nettyrpc.common.utils.SPIUtil;
import com.xiaobai.nettyrpc.common.dto.TransferDTO;
import com.xiaobai.nettyrpc.provider.processor.ProviderPreProcessor;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 异步处理类
 *
 * @author yinzhaojing
 * @date 2022-06-28 11:53:30
 */
public class AsyncProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AsyncProcessor.class);

    @Async
    public void process(ChannelHandlerContext ctx, Object msg, List<String> providerPreProcessors,
                        List<String> providerPostProcessors) {
        TransferDTO requestDTO = (TransferDTO) msg;
        TransferDTO responseDTO = new TransferDTO();
        responseDTO.copyRequestValue(requestDTO);
        // 判断是否是心跳消息
        if (StringUtils.equals(CommonConstants.HEART_BEAT, requestDTO.getMethodName())) {
            responseDTO.setResponseCode(CommonConstants.SUCCESS_CODE);
        } else {
            // 使用SPI机制加载配置文件中指定的处理链做前置处理
            if (null != providerPreProcessors && !providerPreProcessors.isEmpty()) {
                List<ProviderPreProcessor> preProcessorList = SPIUtil.getObjects(providerPreProcessors,
                        ProviderPreProcessor.class);
                try {
                    for (ProviderPreProcessor providerPreProcessor : preProcessorList) {
                        providerPreProcessor.doPreProcess(requestDTO);
                    }
                } catch (Exception e) {
                    logger.error("do pre processor exception:", e);
                    responseDTO.setResponseCode(CommonConstants.ERROR_CODE);
                    responseDTO.setResponseMessage(e.getMessage());
                }
            }
            try {
                // 反射获取调用接口的实现类
                String interfaceName = requestDTO.getInterfaceName();
                String methodName = requestDTO.getMethodName();
                String group = StringUtils.isBlank(requestDTO.getServiceGroup()) ? CommonConstants.DEFAULT
                        : requestDTO.getServiceGroup();
                Class<?>[] parameterTypes = requestDTO.getParameterTypes();
                Object[] params = requestDTO.getParams();
                logger.info("accept remote call,consumer name:{},interface:{},method:{},request id:{}",
                        requestDTO.getConsumerName(), interfaceName, methodName, requestDTO.getRequestId());
                // 缓存中获取对应的实现类信息
                ProviderService providerService = ProviderServiceCache.get(interfaceName, group);
                if (null == providerService) {
                    throw new RemoteCallException("no impl find,interface:" + interfaceName);
                }
                Class<?> clazz = Class.forName(providerService.getImplName());
                Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
                Object result = method.invoke(clazz.newInstance(), params);
                responseDTO.setResponseCode(CommonConstants.SUCCESS_CODE);
                responseDTO.setResponseMessage(CommonConstants.SUCCESS_MESSAGE);
                responseDTO.setProviderName(providerService.getProviderName());
                responseDTO.setResult(result);
                // 使用SPI机制加载配置文件中指定的处理链做前置处理
                if (null != providerPostProcessors && !providerPostProcessors.isEmpty()) {
                    List<com.xiaobai.nettyrpc.provider.processor.ProviderPostProcessor> postProcessorList =
                            SPIUtil.getObjects(providerPostProcessors,
                                    com.xiaobai.nettyrpc.provider.processor.ProviderPostProcessor.class);
                    try {
                        for (com.xiaobai.nettyrpc.provider.processor.ProviderPostProcessor providerPostProcessor
                                : postProcessorList) {
                            providerPostProcessor.doPostProcess(responseDTO);
                        }
                    } catch (Exception e) {
                        logger.error("do post processor exception:", e);
                        responseDTO.setResponseCode(CommonConstants.ERROR_CODE);
                        responseDTO.setResponseMessage(e.getMessage());
                    }
                }
                logger.info("remote call success,client address:{},request id:{}", ctx.channel().remoteAddress(),
                        requestDTO.getRequestId());
            } catch (Exception e) {
                logger.error("remote call exception,request id:{}:", requestDTO.getRequestId(), e);
                responseDTO.setResponseCode(CommonConstants.ERROR_CODE);
                responseDTO.setResponseMessage(e.getMessage());
            }
        }
        ctx.channel().write(responseDTO);
        // 使用\r\n分隔消息
        ctx.channel().writeAndFlush(CommonConstants.LINE_DELIMITER);
    }
}
