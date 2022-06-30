package com.xiaobai.nettyrpc.provider.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiaobai.nettyrpc.common.constants.CommonConstants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务缓存
 *
 * @author yinzhaojing
 * @date 2022-06-24 15:06:40
 */
public class ProviderServiceCache {
    private static final Map<String, ProviderService> CACHE = new ConcurrentHashMap<>();

    /**
     * 添加缓存
     * @param key 接口全限定类名:group
     * @param providerService 实现类信息
     */
    public static void add(String key, ProviderService providerService) {
        CACHE.put(key, providerService);
    }

    /**
     * 获取提供者service信息
     * @param key 接口全限定类名:group
     * @return 实现类名
     */
    public static ProviderService get(String key) {
        if (CACHE.containsKey(key)) {
            return CACHE.get(key);
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

    /**
     * 获取所有service
     * @return service列表
     */
    public static JSONArray getServices() {
        JSONArray services = new JSONArray();
        for (Map.Entry<String, ProviderService> entry : CACHE.entrySet()) {
            ProviderService providerService = entry.getValue();
            JSONObject service = new JSONObject();
            service.put(CommonConstants.INTERFACE, providerService.getInterfaceName());
            service.put(CommonConstants.GROUP, providerService.getGroup());
            service.put(CommonConstants.IMPL, providerService.getImplName());
            services.add(service);
        }
        return services;
    }
}
