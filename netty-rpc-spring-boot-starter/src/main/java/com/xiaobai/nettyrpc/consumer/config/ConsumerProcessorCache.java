package com.xiaobai.nettyrpc.consumer.config;

import com.xiaobai.nettyrpc.common.utils.SPIUtil;
import com.xiaobai.nettyrpc.consumer.processor.ConsumerPreProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Consumer前置后置处理器缓存
 *
 * @author yinzhaojing
 * @date 2022-07-12 16:57:49
 */
public class ConsumerProcessorCache {
    private static List<ConsumerPreProcessor> PRE_PROCESSORS = null;
    private static List<com.xiaobai.nettyrpc.consumer.processor.ConsumerPostProcessor> POST_PROCESSORS = null;

    /**
     * 获取Consumer前置处理器列表
     * @param consumerPreProcessors 前置处理器名称
     * @return 前置处理器列表
     */
    public static List<ConsumerPreProcessor> getConsumerPreProcessors(List<String> consumerPreProcessors) {
        if (null == PRE_PROCESSORS) {
            initPreProcessor(consumerPreProcessors);
        }
        return PRE_PROCESSORS;
    }

    /**
     * 获取Consumer后置处理器列表
     * @param consumerPostProcessors 后置处理器名称
     * @return 后置处理器列表
     */
    public static List<com.xiaobai.nettyrpc.consumer.processor.ConsumerPostProcessor> getConsumerPostProcessors(
            List<String> consumerPostProcessors) {
        if (null == POST_PROCESSORS) {
            initPostProcessor(consumerPostProcessors);
        }
        return POST_PROCESSORS;
    }

    /**
     * 初始化前置处理器列表
     * @param consumerPreProcessors 前置处理器名称
     */
    private synchronized static void initPreProcessor(List<String> consumerPreProcessors) {
        PRE_PROCESSORS = new ArrayList<>();
        // 使用SPI机制加载配置文件中指定的前置处理链
        if (null != consumerPreProcessors && !consumerPreProcessors.isEmpty()) {
            PRE_PROCESSORS = SPIUtil.getObjects(consumerPreProcessors, ConsumerPreProcessor.class);
        }
    }

    /**
     * 初始化后置处理器列表
     * @param consumerPostProcessors 后置处理器名称
     */
    private synchronized static void initPostProcessor(List<String> consumerPostProcessors) {
        POST_PROCESSORS = new ArrayList<>();
        // 使用SPI机制加载配置文件中指定的后置处理链
        if (null != consumerPostProcessors && !consumerPostProcessors.isEmpty()) {
            POST_PROCESSORS = SPIUtil.getObjects(consumerPostProcessors,
                    com.xiaobai.nettyrpc.consumer.processor.ConsumerPostProcessor.class);
        }
    }
}
