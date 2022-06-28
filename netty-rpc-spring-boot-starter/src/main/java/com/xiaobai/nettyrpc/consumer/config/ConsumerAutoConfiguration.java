package com.xiaobai.nettyrpc.consumer.config;

import com.xiaobai.nettyrpc.common.properties.NettyRpcProperties;
import com.xiaobai.nettyrpc.consumer.heartbeat.ClientHeartBeat;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Bean
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
