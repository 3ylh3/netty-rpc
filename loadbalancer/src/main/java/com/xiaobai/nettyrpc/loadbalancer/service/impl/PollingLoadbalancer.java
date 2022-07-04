package com.xiaobai.nettyrpc.loadbalancer.service.impl;

import com.xiaobai.nettyrpc.loadbalancer.entity.RemoteService;
import com.xiaobai.nettyrpc.loadbalancer.service.Loadbalancer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 轮询负载均衡
 *
 * @author yinzhaojing
 * @date 2022-07-04 15:44:40
 */
public class PollingLoadbalancer implements Loadbalancer {
    private static final String CACHE_KEY_DELIMITER = "|";
    private static final Map<String, Integer> INDEX = new HashMap<>();

    /**
     * 轮询负载均衡
     * @param list 远程服务列表
     * @return 远程服务实例
     */
    @Override
    public RemoteService selectRemoteService(String providerName, String interfaceName, String group,
                                             List<RemoteService> list) {
        if (null == list || list.isEmpty()) {
            return null;
        }
        String key = providerName + CACHE_KEY_DELIMITER + interfaceName + CACHE_KEY_DELIMITER + group;
        int index = 0;
        synchronized (INDEX) {
            if (INDEX.containsKey(key)) {
                index = (INDEX.get(key) + 1) % list.size();
            }
            RemoteService remoteService = list.get(index);
            INDEX.put(key, index);
            return remoteService;
        }
    }
}
