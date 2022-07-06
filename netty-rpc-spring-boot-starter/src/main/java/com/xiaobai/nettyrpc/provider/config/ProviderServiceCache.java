package com.xiaobai.nettyrpc.provider.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiaobai.nettyrpc.common.constants.CommonConstants;
import org.apache.commons.lang3.StringUtils;

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
    private static final Map<String, List<ProviderService>> CACHE = new ConcurrentHashMap<>();

    /**
     * 添加缓存
     * @param key 接口全限定类名
     * @param providerService 实现类信息
     */
    public static void add(String key, ProviderService providerService) {
        synchronized (CACHE) {
            List<ProviderService> list = null;
            if (CACHE.containsKey(key)) {
                list = CACHE.get(key);
            } else {
                list = new ArrayList<>();
            }
            list.add(providerService);
            CACHE.put(key, list);
        }
    }

    /**
     * 获取提供者service信息
     * @param key 接口全限定类名
     * @param group 接口组
     * @return 实现类名
     */
    public static ProviderService get(String key, String group) {
        if (CACHE.containsKey(key)) {
            List<ProviderService> list = CACHE.get(key);
            for (ProviderService providerService : list) {
                if (StringUtils.equals(CommonConstants.DEFAULT, group)
                        || StringUtils.equals(group, providerService.getGroup())) {
                    return providerService;
                }
            }
            return null;
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
        for (Map.Entry<String, List<ProviderService>> entry : CACHE.entrySet()) {
            List<ProviderService> providerServices = entry.getValue();
            for (ProviderService providerService : providerServices) {
                JSONObject service = new JSONObject();
                service.put(CommonConstants.INTERFACE, providerService.getInterfaceName());
                service.put(CommonConstants.GROUP, providerService.getGroup());
                service.put(CommonConstants.IMPL, providerService.getImplName());
                service.put(CommonConstants.WEIGHT, providerService.getWeight());
                services.add(service);
            }
        }
        return services;
    }
}
