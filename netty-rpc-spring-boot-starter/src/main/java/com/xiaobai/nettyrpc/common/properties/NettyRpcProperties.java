package com.xiaobai.nettyrpc.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

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

}
