package com.xiaobai.nettyrpc.provider.config;

import com.xiaobai.nettyrpc.common.constants.CommonConstants;
import com.xiaobai.nettyrpc.common.properties.NettyRpcProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 提供者自动装配类
 *
 * @author yinzhaojing
 * @date 2022-06-24 15:00:04
 */
@Configuration
@EnableConfigurationProperties(NettyRpcProperties.class)
@EnableAsync
public class ProviderAutoConfiguration implements AsyncConfigurer {
    @Autowired
    private NettyRpcProperties nettyRpcProperties;

    @Bean
    @ConditionalOnMissingBean(ProviderPostProcessor.class)
    public ProviderPostProcessor initProviderPostProcessor() {
        return new ProviderPostProcessor();
    }

    @Bean
    @DependsOn({"namingService"})
    @ConditionalOnMissingBean(NettyServer.class)
    public NettyServer initNettyServer() {
        return new NettyServer();
    }

    @Bean("asyncProcessor")
    @ConditionalOnMissingBean(AsyncProcessor.class)
    public AsyncProcessor initAsyncProcessor() {
        return new AsyncProcessor();
    }

    @Bean
    @ConditionalOnMissingBean(NettyServerHandler.class)
    @DependsOn({"asyncProcessor"})
    public NettyServerHandler initNettyServerHandler() {
        return new NettyServerHandler();
    }

    @Bean
    @ConditionalOnMissingBean(ThreadPoolMetric.class)
    public ThreadPoolMetric initThreadPoolMetric() {
        return new ThreadPoolMetric();
    }

    /**
     * 初始化提供者处理线程池
     * @return
     */
    @Override
    @Bean("provider-process")
    public Executor getAsyncExecutor() {
        // 如果有远程服务需要暴露，则启动线程池
        if (ProviderServiceCache.isEmpty()) {
            return null;
        }
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(null == nettyRpcProperties.getProviderCorePoolSize()
                ? CommonConstants.DEFAULT_PROVIDER_CORE_POOL_SIZE : nettyRpcProperties.getProviderCorePoolSize());
        executor.setMaxPoolSize(null == nettyRpcProperties.getProviderMaxPoolSize()
                ? CommonConstants.DEFAULT_PROVIDER_MAX_POOL_SIZE : nettyRpcProperties.getProviderMaxPoolSize());
        executor.setQueueCapacity(null == nettyRpcProperties.getProviderQueueCapacity()
                ? CommonConstants.DEFAULT_PROVIDER_QUEUE_CAPACITY : nettyRpcProperties.getProviderQueueCapacity());
        executor.setKeepAliveSeconds(null == nettyRpcProperties.getProviderKeepAliveSeconds()
                ? CommonConstants.DEFAULT_PROVIDER_KEEP_ALIVE_SECONDS : nettyRpcProperties.getProviderKeepAliveSeconds());
        executor.setThreadNamePrefix(CommonConstants.PROVIDER_PROCESS_THREAD_NAME_PREFIX);
        // 当线程数量已经达到maxSize的时候，如何处理新任务
        // CallerRunsPolicy：不在新线程中执行任务，而是由调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

}
