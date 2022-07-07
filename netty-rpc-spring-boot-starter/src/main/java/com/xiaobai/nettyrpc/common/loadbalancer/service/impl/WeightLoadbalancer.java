package com.xiaobai.nettyrpc.common.loadbalancer.service.impl;

import com.xiaobai.nettyrpc.common.entity.RemoteService;
import com.xiaobai.nettyrpc.common.loadbalancer.service.Loadbalancer;

import java.util.List;
import java.util.Random;

/**
 * 随机权重负载均衡
 *
 * @author yinzhaojing
 * @date 2022-07-06 11:02:54
 */
public class WeightLoadbalancer implements Loadbalancer {

    /**
     * 根据负载均衡策略选择一个远程服务实例
     * @param providerName 提供者名称
     * @param interfaceName 远程调用接口名
     * @param group 接口组
     * @param list 远程服务列表
     * @return 远程服务实例
     */
    @Override
    public RemoteService selectRemoteService(String providerName, String interfaceName, String group,
                                             List<RemoteService> list) {
        if (null == list || list.isEmpty()) {
            return null;
        }
        // 总权重
        int totalWeight = 0;
        for (RemoteService remoteService : list) {
            totalWeight += remoteService.getWeight();
        }
        Random random = new Random(System.currentTimeMillis());
        int randomWithWeight = random.nextInt(totalWeight);
        int tmp = 0;
        for (RemoteService remoteService : list) {
            tmp += remoteService.getWeight();
            if (tmp > randomWithWeight) {
                return remoteService;
            }
        }
        return null;
    }
}
