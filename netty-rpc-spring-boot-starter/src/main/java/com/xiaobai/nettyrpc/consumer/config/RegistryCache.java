package com.xiaobai.nettyrpc.consumer.config;

import com.alibaba.nacos.api.naming.NamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 注册中心缓存
 *
 * @author yinzhaojing
 * @date 2022-06-30 16:53:17
 */
public class RegistryCache {

    private static final Logger logger = LoggerFactory.getLogger(RegistryCache.class);
    private static final Map<String, List<RemoteService>> CACHE = new HashMap<>();

    @Autowired
    private NamingService namingService;

    /**
     * 初始化缓存
     */
    public void init() {

    }
}
