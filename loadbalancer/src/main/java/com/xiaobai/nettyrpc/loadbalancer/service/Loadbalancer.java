package com.xiaobai.nettyrpc.loadbalancer.service;

import com.xiaobai.nettyrpc.loadbalancer.entity.RemoteService;

import java.util.List;

/**
 * 负载均衡接口
 *
 * @author yinzhaojing
 * @date 2022-07-04 11:03:17
 */
public interface Loadbalancer {
    /**
     * 根据负载均衡策略选择一个远程服务实例
     * @param providerName 提供者名称
     * @param interfaceName 远程调用接口名
     * @param group 接口组
     * @param list 远程服务列表
     * @return 远程服务实例
     */
    RemoteService selectRemoteService(String providerName, String interfaceName, String group, List<RemoteService> list);
}
