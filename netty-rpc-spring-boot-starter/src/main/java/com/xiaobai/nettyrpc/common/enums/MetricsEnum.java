package com.xiaobai.nettyrpc.common.enums;

/**
 * metric枚举
 *
 * @author yinzhaojing
 * @date 2022-07-08 17:31:34
 */
public enum MetricsEnum {
    REMOTE_CALL_TOTAL("remote_call_total", "远程调用次数"),
    REMOTE_CALL_TIME_CONSUME_RANGE("remote_call_time_consume_range", "远程调用耗时分布"),
    HEARTBEAT_TOTAL("heartbeat_total", "心跳次数"),
    RECEIVE_REMOTE_CALL_TOTAL("receive_remote_call_total", "接收远程调用次数"),
    PROCESS_REMOTE_CALL_TIME_CONSUME_RANGE("process_remote_call_time_consume_range", "处理远程调用耗时分布"),
    PROVIDER_PROCESS_EXECUTOR_ACTIVE_THREADS("provider_process_executor_active_threads", "提供者处理线程池活跃线程数"),
    PROVIDER_PROCESS_EXECUTOR_POOL_SIZE("provider_process_executor_pool_size", "提供者处理线程池当前线程数"),
    PROVIDER_PROCESS_EXECUTOR_CORE_POOL_SIZE("provider_process_executor_core_pool_size", "提供者处理线程池核心线程数"),
    PROVIDER_PROCESS_EXECUTOR_MAX_POOL_SIZE("provider_process_executor_max_pool_size", "提供者处理线程池最大线程数"),
    PROVIDER_PROCESS_EXECUTOR_TASK_NUMBERS("provider_process_executor_task_count", "提供者处理线程池任务队列堆积任务个数"),
    PROVIDER_PROCESS_EXECUTOR_QUEUE_REMAINING_CAPACITY("provider_process_executor_queue_remaining_capacity", "提供者处理线程池任务队列剩余容量"),
    PROVIDER_PROCESS_EXECUTOR_COMPLETED_TASK_COUNT("provider_process_executor_completed_task_count", "提供者处理线程池已完成任务数量"),
    PROVIDER_PROCESS_EXECUTOR_KEEP_ALIVE_SECONDS("provider_process_executor_keep_alive_seconds", "提供者处理线程池空闲线程保留时长（秒）");

    private final String name;
    private final String description;

    MetricsEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return this.name;
    }
}
