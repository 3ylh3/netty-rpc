package com.xiaobai.nettyrpc.provider.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiaobai.nettyrpc.common.constants.CommonConstants;
import com.xiaobai.nettyrpc.common.entity.Collector;
import com.xiaobai.nettyrpc.common.enums.MetricsEnum;
import com.xiaobai.nettyrpc.common.exceptions.RateLimitException;
import com.xiaobai.nettyrpc.common.exceptions.RemoteCallException;
import com.xiaobai.nettyrpc.common.dto.TransferDTO;
import com.xiaobai.nettyrpc.common.utils.TimeUtil;
import com.xiaobai.nettyrpc.provider.processor.ProviderPreProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private Collector collector;
    private static final Logger logger = LoggerFactory.getLogger(AsyncProcessor.class);

    @Async
    public void process(ChannelHandlerContext ctx, Object msg, List<String> providerPreProcessors,
                        String providerPreProcessorsParams, List<String> providerPostProcessors,
                        String providerPostProcessorsParams) {
        TransferDTO requestDTO = (TransferDTO) msg;
        TransferDTO responseDTO = new TransferDTO();
        responseDTO.copyRequestValue(requestDTO);
        try {
            // 判断是否是心跳消息
            if (StringUtils.equals(CommonConstants.HEART_BEAT, requestDTO.getMethodName())) {
                responseDTO.setResponseCode(CommonConstants.SUCCESS_CODE);
            } else {
                long startTime = TimeUtil.currentTimeMillis();
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
                    responseDTO.setProviderName(providerService.getProviderName());
                    responseDTO.setServiceGroup(providerService.getGroup());
                    // 执行前置处理
                    JSONObject preProcessorsParams = new JSONObject();
                    if (!StringUtils.isBlank(providerPreProcessorsParams)) {
                        preProcessorsParams = JSON.parseObject(providerPreProcessorsParams);
                    }
                    List<ProviderPreProcessor> preProcessorList = ProviderProcessorCache
                            .getPreProcessors(providerPreProcessors);
                    try {
                        int i = 1;
                        for (ProviderPreProcessor providerPreProcessor : preProcessorList) {
                            providerPreProcessor.doPreProcess(requestDTO,
                                    preProcessorsParams.getJSONObject(String.valueOf(i)));
                            i++;
                        }
                    } catch (Exception e) {
                        logger.error("do pre processor exception:", e);
                        responseDTO.setResponseCode(CommonConstants.ERROR_CODE);
                        responseDTO.setResponseMessage(e.getMessage());
                        String type = CommonConstants.FAIL;
                        if (e instanceof RateLimitException) {
                            type = CommonConstants.RATE_LIMIT;
                        }
                        // 记录失败次数以及耗时
                        recordMetric(startTime, ctx.channel().remoteAddress().toString(),
                                responseDTO.getInterfaceName(), providerService.getImplName(),
                                providerService.getGroup(), responseDTO.getMethodName(),
                                type);
                        return;
                    }
                    Class<?> clazz = Class.forName(providerService.getImplName());
                    Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
                    Object result = method.invoke(clazz.newInstance(), params);
                    responseDTO.setResponseCode(CommonConstants.SUCCESS_CODE);
                    responseDTO.setResponseMessage(CommonConstants.SUCCESS_MESSAGE);
                    responseDTO.setResult(result);
                    // 执行后置处理
                    JSONObject postProcessorsParams = new JSONObject();
                    if (!StringUtils.isBlank(providerPostProcessorsParams)) {
                        postProcessorsParams = JSON.parseObject(providerPostProcessorsParams);
                    }
                    List<com.xiaobai.nettyrpc.provider.processor.ProviderPostProcessor> postProcessorList =
                            ProviderProcessorCache.getPostProcessors(providerPostProcessors);
                    try {
                        int i = 1;
                        for (com.xiaobai.nettyrpc.provider.processor.ProviderPostProcessor providerPostProcessor
                                : postProcessorList) {
                            providerPostProcessor.doPostProcess(responseDTO,
                                    postProcessorsParams.getJSONObject(String.valueOf(i)));
                            i++;
                        }
                    } catch (Exception e) {
                        logger.error("do post processor exception:", e);
                        responseDTO.setResponseCode(CommonConstants.ERROR_CODE);
                        responseDTO.setResponseMessage(e.getMessage());
                        // 记录失败次数以及耗时
                        recordMetric(startTime, ctx.channel().remoteAddress().toString(),
                                responseDTO.getInterfaceName(), providerService.getImplName(),
                                responseDTO.getServiceGroup(), responseDTO.getMethodName(), CommonConstants.FAIL);
                        return;
                    }
                    logger.info("remote call success,client address:{},request id:{}", ctx.channel().remoteAddress(),
                            requestDTO.getRequestId());
                    // 记录成功次数及耗时
                    recordMetric(startTime, ctx.channel().remoteAddress().toString(), responseDTO.getInterfaceName(),
                            providerService.getImplName(), responseDTO.getServiceGroup(), responseDTO.getMethodName(),
                            CommonConstants.SUCCESS);
                } catch (Exception e) {
                    logger.error("remote call exception,request id:{}:", requestDTO.getRequestId(), e);
                    responseDTO.setResponseCode(CommonConstants.ERROR_CODE);
                    responseDTO.setResponseMessage(e.getMessage());
                    // 记录失败次数以及耗时
                    recordMetric(startTime, ctx.channel().remoteAddress().toString(), responseDTO.getInterfaceName(),
                            "", responseDTO.getServiceGroup(), responseDTO.getMethodName(), CommonConstants.FAIL);
                }
            }
        } finally {
            ctx.channel().write(responseDTO);
            // 使用\r\n分隔消息
            ctx.channel().writeAndFlush(CommonConstants.LINE_DELIMITER);
        }
    }

    /**
     * 记录次数及耗时
     * @param startTime 开始时间
     * @param clientAddress 客户端地址
     * @param interfaceName 接口名
     * @param impl 实现类
     * @param group 实现类组
     * @param method 方法名
     * @param type 类型
     */
    private void recordMetric(long startTime, String clientAddress, String interfaceName, String impl, String group,
                              String method, String type) {
        if (!collector.isEmpty()) {
            long endTime = TimeUtil.currentTimeMillis();
            ((Counter) collector.get(MetricsEnum.RECEIVE_REMOTE_CALL_TOTAL.getName())).labels(clientAddress,
                    interfaceName, impl, group, method, type).inc();
            ((Histogram) collector.get(MetricsEnum.PROCESS_REMOTE_CALL_TIME_CONSUME_RANGE.getName()))
                    .labels(clientAddress, interfaceName, impl, group, method).observe(endTime - startTime);
        }
    }
}
