package com.xiaobai.nettyrpc.common.entity;

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

    public void copyFrom(RemoteService target) {
        this.providerName = target.getProviderName();
        this.group = target.getGroup();
        this.ip = target.getIp();
        this.port = target.getPort();
        this.weight = target.getWeight();
        this.isHealthy = target.getIsHealthy();
    }
}
