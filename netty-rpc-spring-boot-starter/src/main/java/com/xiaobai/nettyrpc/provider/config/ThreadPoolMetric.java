package com.xiaobai.nettyrpc.provider.config;

import com.xiaobai.nettyrpc.common.entity.Collector;
import com.xiaobai.nettyrpc.common.enums.MetricsEnum;
import com.xiaobai.nettyrpc.common.properties.NettyRpcProperties;
import io.prometheus.client.Gauge;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 提供者处理线程池指标
 *
 * @author yinzhaojing
 * @date 2022-07-11 17:31:04
 */
public class ThreadPoolMetric {

    @Autowired
    private Collector collector;
    @Autowired
    @Qualifier("provider-process")
    private Executor executor;
    @Autowired
    private NettyRpcProperties nettyRpcProperties;
    @Value("${spring.application.name}")
    private String applicationName;

    @Scheduled(cron = "0/10 * * * * ? ")
    public void exportExecutorMetric() {
        if (!collector.isEmpty()) {
            String providerName = StringUtils.isBlank(nettyRpcProperties.getName()) ? applicationName
                    : nettyRpcProperties.getName();
            ((Gauge) collector.get(MetricsEnum.PROVIDER_PROCESS_EXECUTOR_CORE_POOL_SIZE.getName())).labels(providerName)
                    .set(((ThreadPoolTaskExecutor) executor).getCorePoolSize());
            ((Gauge) collector.get(MetricsEnum.PROVIDER_PROCESS_EXECUTOR_MAX_POOL_SIZE.getName())).labels(providerName)
                    .set(((ThreadPoolTaskExecutor) executor).getMaxPoolSize());
            ((Gauge) collector.get(MetricsEnum.PROVIDER_PROCESS_EXECUTOR_ACTIVE_THREADS.getName())).labels(providerName)
                    .set(((ThreadPoolTaskExecutor) executor).getThreadPoolExecutor().getActiveCount());
            ((Gauge) collector.get(MetricsEnum.PROVIDER_PROCESS_EXECUTOR_POOL_SIZE.getName())).labels(providerName)
                    .set(((ThreadPoolTaskExecutor) executor).getThreadPoolExecutor().getPoolSize());
            ((Gauge) collector.get(MetricsEnum.PROVIDER_PROCESS_EXECUTOR_TASK_NUMBERS.getName())).labels(providerName)
                    .set(((ThreadPoolTaskExecutor) executor).getThreadPoolExecutor().getQueue().size());
            ((Gauge) collector.get(MetricsEnum.PROVIDER_PROCESS_EXECUTOR_QUEUE_REMAINING_CAPACITY.getName()))
                    .labels(providerName).set(((ThreadPoolTaskExecutor) executor).getThreadPoolExecutor().getQueue()
                            .remainingCapacity());
            ((Gauge) collector.get(MetricsEnum.PROVIDER_PROCESS_EXECUTOR_COMPLETED_TASK_COUNT.getName()))
                    .labels(providerName).set(((ThreadPoolTaskExecutor) executor).getThreadPoolExecutor()
                            .getCompletedTaskCount());
            ((Gauge) collector.get(MetricsEnum.PROVIDER_PROCESS_EXECUTOR_KEEP_ALIVE_SECONDS.getName()))
                    .labels(providerName).set(((ThreadPoolTaskExecutor) executor).getKeepAliveSeconds());
        }
    }
}
