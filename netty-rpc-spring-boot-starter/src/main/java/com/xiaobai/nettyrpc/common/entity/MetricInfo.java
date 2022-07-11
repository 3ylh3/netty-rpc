package com.xiaobai.nettyrpc.common.entity;

import lombok.Data;

/**
 * 指标信息
 *
 * @author yinzhaojing
 * @date 2022-07-08 16:36:25
 */
@Data
public class MetricInfo {
    /**
     * 指标名
     */
    private String name;
    /**
     * 指标描述
     */
    private String description;
    /**
     * 类型：Counter,Gauge,Histogram,Summary
     */
    private String type;
    /**
     * labels
     */
    private String[] labels;
    /**
     * buckets
     */
    private double[] buckets;
}
