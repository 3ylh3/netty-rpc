package com.xiaobai.nettyrpc.loadbalancer.service.impl;

import com.xiaobai.nettyrpc.loadbalancer.entity.RemoteService;
import com.xiaobai.nettyrpc.loadbalancer.service.Loadbalancer;

import java.util.List;
import java.util.Random;

/**
 * 随机负载均衡实现类
 *
 * @author yinzhaojing
 * @date 2022-07-04 11:14:43
 */
public class RandomLoadbalancer implements Loadbalancer {

    @Override
    public RemoteService selectRemoteService(List<RemoteService> list) {
        if (null != list && !list.isEmpty()) {
            Random random = new Random(System.currentTimeMillis());
            int number = random.nextInt(list.size());
            return list.get(number);
        } else {
            return null;
        }
    }
}
