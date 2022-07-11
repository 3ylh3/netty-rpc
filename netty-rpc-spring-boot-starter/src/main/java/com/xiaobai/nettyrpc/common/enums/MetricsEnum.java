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
    HEARTBEAT_TOTAL("heartbeat_total", "心跳次数");

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
