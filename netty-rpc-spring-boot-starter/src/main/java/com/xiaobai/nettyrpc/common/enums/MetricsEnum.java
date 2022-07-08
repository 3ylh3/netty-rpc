package com.xiaobai.nettyrpc.common.enums;

/**
 * metric枚举
 *
 * @author yinzhaojing
 * @date 2022-07-08 17:31:34
 */
public enum MetricsEnum {
    REMOTE_CALL_TOTAL("remote_call_total", "远程调用次数");

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
