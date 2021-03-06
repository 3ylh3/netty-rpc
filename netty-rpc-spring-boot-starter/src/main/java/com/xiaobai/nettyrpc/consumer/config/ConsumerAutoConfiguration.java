package com.xiaobai.nettyrpc.consumer.config;

import com.alibaba.nacos.api.naming.NamingService;
import com.xiaobai.nettyrpc.common.properties.NettyRpcProperties;
import com.xiaobai.nettyrpc.consumer.heartbeat.ClientHeartBeat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * consumer自动装配类
 *
 * @author yinzhaojing
 * @date 2022-06-22 19:10:14
 */
@Configuration
@EnableConfigurationProperties(NettyRpcProperties.class)
@EnableScheduling
public class ConsumerAutoConfiguration {
    @Autowired
    private NamingService namingService;
    @Autowired
    private NettyRpcProperties nettyRpcProperties;

    @Bean("registryCache")
    @DependsOn({"namingService"})
    @ConditionalOnMissingBean(RegistryCache.class)
    public RegistryCache initRegistryCache() throws Exception {
        RegistryCache registryCache = new RegistryCache(namingService, nettyRpcProperties);
        registryCache.init();
        return registryCache;
    }

    @Bean
    @DependsOn({"registryCache", "collector"})
    @ConditionalOnMissingBean(ConsumerPostProcessor.class)
    public ConsumerPostProcessor initConsumerPostProcessor() throws Exception {
        return new ConsumerPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean(ClientHeartBeat.class)
    public ClientHeartBeat initClientHeartBeat() {
        return new ClientHeartBeat();
    }
}
