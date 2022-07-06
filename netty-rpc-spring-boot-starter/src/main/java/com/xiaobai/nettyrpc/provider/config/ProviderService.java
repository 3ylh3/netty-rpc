package com.xiaobai.nettyrpc.provider.config;

import lombok.Data;

/**
 * 提供者service
 *
 * @author yinzhaojing
 * @date 2022-06-24 15:08:52
 */
@Data
public class ProviderService {
    /**
     * 提供者名称
     */
    private String providerName;
    /**
     * 接口全限定名
     */
    private String interfaceName;
    /**
     * 组
     */
    private String group;
    /**
     * 实现类名称
     */
    private String implName;
    /**
     * 权重
     */
    private Integer weight;
}
