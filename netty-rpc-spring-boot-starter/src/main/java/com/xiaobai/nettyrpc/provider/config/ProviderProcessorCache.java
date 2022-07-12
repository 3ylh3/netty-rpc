package com.xiaobai.nettyrpc.provider.config;

import com.xiaobai.nettyrpc.common.utils.SPIUtil;
import com.xiaobai.nettyrpc.provider.processor.ProviderPreProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Provider前置后置处理器缓存
 *
 * @author yinzhaojing
 * @date 2022-07-12 19:09:28
 */
public class ProviderProcessorCache {

    private static List<ProviderPreProcessor> PRE_PROCESSORS = null;
    private static List<com.xiaobai.nettyrpc.provider.processor.ProviderPostProcessor> POST_PROCESSORS = null;

    /**
     * 获取Provider前置处理器列表
     * @param providerPreProcessors 前置处理器名称
     * @return 前置处理器列表
     */
    public static List<ProviderPreProcessor> getPreProcessors(List<String> providerPreProcessors) {
        if (null == PRE_PROCESSORS) {
            initPreProcessors(providerPreProcessors);
        }
        return PRE_PROCESSORS;
    }

    /**
     * 获取Provider后置处理器列表
     * @param providerPostProcessors 后置处理器名称
     * @return 后置处理器列表
     */
    public static List<com.xiaobai.nettyrpc.provider.processor.ProviderPostProcessor> getPostProcessors(
            List<String> providerPostProcessors) {
        if (null == POST_PROCESSORS) {
            initPostProcessors(providerPostProcessors);
        }
        return POST_PROCESSORS;
    }

    /**
     * 初始化前置处理器
     * @param providerPreProcessors 前置处理器名称
     */
    private synchronized static void initPreProcessors(List<String> providerPreProcessors) {
        PRE_PROCESSORS = new ArrayList<>();
        // 使用SPI机制加载配置文件中指定的前置处理链
        if (null != providerPreProcessors && !providerPreProcessors.isEmpty()) {
            PRE_PROCESSORS = SPIUtil.getObjects(providerPreProcessors,
                    ProviderPreProcessor.class);
        }
    }

    /**
     * 初始化后置处理器
     * @param providerPostProcessors 后置处理器名称
     */
    private synchronized static void initPostProcessors(List<String> providerPostProcessors) {
        POST_PROCESSORS = new ArrayList<>();
        // 使用SPI机制加载配置文件中指定的后置处理链
        if (null != providerPostProcessors && !providerPostProcessors.isEmpty()) {
            POST_PROCESSORS = SPIUtil.getObjects(providerPostProcessors,
                    com.xiaobai.nettyrpc.provider.processor.ProviderPostProcessor.class);
        }
    }
}
