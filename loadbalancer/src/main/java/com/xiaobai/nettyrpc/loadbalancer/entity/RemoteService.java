package com.xiaobai.nettyrpc.loadbalancer.entity;

import lombok.Data;

/**
 * 远程服务端实体类
 *
 * @author yinzhaojing
 * @date 2022-06-23 15:01:50
 */
@Data
public class RemoteService {
    /**
     * 提供者名称
     */
    private String providerName;
    /**
     * 接口组
     */
    private String group;
    /**
     * ip地址
     */
    private String ip;
    /**
     * 端口
     */
    private Integer port;
    /**
     * 权重
     */
    private Integer weight;
    /**
     * 是否健康
     */
    private Boolean isHealthy;
}
