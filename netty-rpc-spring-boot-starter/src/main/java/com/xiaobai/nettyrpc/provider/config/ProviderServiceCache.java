package com.xiaobai.nettyrpc.provider.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务缓存
 *
 * @author yinzhaojing
 * @date 2022-06-24 15:06:40
 */
public class ProviderServiceCache {
    private static final Map<String, List<ProviderService>> CACHE = new ConcurrentHashMap<>(8);

    /**
     * 添加缓存
     * @param interfaceName 接口全限定类名
     * @param providerService 实现类信息
     */
    public static void add(String interfaceName, ProviderService providerService) {
        synchronized (CACHE) {
            List<ProviderService> list = null;
            if (CACHE.containsKey(interfaceName)) {
                list = CACHE.get(interfaceName);
            } else {
                list = new ArrayList<>();
            }
            list.add(providerService);
            CACHE.put(interfaceName, list);
        }
    }

    /**
     * 获取提供者service信息
     * @param interfaceName 接口名
     * @return 实现类名
     */
    public static ProviderService get(String interfaceName) {
        if (CACHE.containsKey(interfaceName)) {
            // TODO 根据group获取实现类名称
            List<ProviderService> list = CACHE.get(interfaceName);
            return list.get(0);


        } else {
            return null;
        }
    }

    /**
     * 判断是否有缓存
     * @return 是否有缓存
     */
    public static boolean isEmpty() {
        return CACHE.isEmpty();
    }
}
