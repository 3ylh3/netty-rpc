package com.xiaobai.nettyrpc.provider.config;

import com.xiaobai.nettyrpc.common.properties.NettyRpcProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 提供者自动装配类
 *
 * @author yinzhaojing
 * @date 2022-06-24 15:00:04
 */
@Configuration
@EnableConfigurationProperties(NettyRpcProperties.class)
public class ProviderAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ProviderPostProcessor.class)
    public ProviderPostProcessor initProviderPostProcessor() {
        return new ProviderPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean(NettyServer.class)
    public NettyServer initNettyServer() throws Exception {
        return new NettyServer();
    }
}
