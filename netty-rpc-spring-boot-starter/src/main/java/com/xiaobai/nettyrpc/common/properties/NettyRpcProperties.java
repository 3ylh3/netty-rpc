package com.xiaobai.nettyrpc.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 配置属性类
 *
 * @author yinzhaojing
 * @date 2022-06-22 18:55:44
 */
@Data
@ConfigurationProperties("netty-rpc")
public class NettyRpcProperties {
    /**
     * 注册至注册中心的服务名
     */
    private String name;
    /**
     * nacos地址
     */
    private String nacosAddress;
    /**
     * 命名空间，用于区分环境，默认public命名空间
     */
    private String namespaceId;
    /**
     * 超时时间，单位秒
     */
    private Integer timeout;
    /**
     * 自定义编码类全限定类名
     */
    private String encodeClassName;
    /**
     * 自定义解码类全限定类名
     */
    private String decodeClassName;
    /**
     * 提供者端口
     */
    private Integer providerPort;
    /**
     * 提供者处理线程池核心线程数
     */
    private Integer providerCorePoolSize;
    /**
     * 提供者处理线程池最大线程数
     */
    private Integer providerMaxPoolSize;
    /**
     * 提供者处理线程池队列长度
     */
    private Integer providerQueueCapacity;
    /**
     * 提供者处理线程池空闲线程存活时间
     */
    private Integer providerKeepAliveSeconds;
    /**
     * 消费者前置处理器全限定类名列表
     */
    private List<String> consumerPreProcessors;
    /**
     * 消费者后置处理器全限定类名列表
     */
    private List<String> consumerPostProcessors;
    /**
     * 消费者前置处理器入参列表
     */
    private String consumerPreProcessorsParams;
    /**
     * 消费者后置处理器入参列表
     */
    private String consumerPostProcessorsParams;
    /**
     * 提供者前置处理器全限定类名列表
     */
    private List<String> providerPreProcessors;
    /**
     * 提供者后置处理器全限定类名列表
     */
    private List<String> providerPostProcessors;
    /**
     * 提供者前置处理器入参列表
     */
    private String providerPreProcessorsParams;
    /**
     * 提供者后置处理器入参列表
     */
    private String providerPostProcessorsParams;
}
