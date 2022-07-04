package com.xiaobai.nettyrpc.consumer.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.xiaobai.nettyrpc.common.constants.CommonConstants;
import com.xiaobai.nettyrpc.common.properties.NettyRpcProperties;
import com.xiaobai.nettyrpc.common.utils.RemoteServiceUtil;
import com.xiaobai.nettyrpc.loadbalancer.entity.RemoteService;

import java.util.ArrayList;
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

    private static final Map<String, List<RemoteService>> CACHE = new HashMap<>();

    private NamingService namingService;
    private NettyRpcProperties nettyRpcProperties;

    public RegistryCache(NamingService namingService, NettyRpcProperties nettyRpcProperties) {
        this.namingService = namingService;
        this.nettyRpcProperties = nettyRpcProperties;
    }

    /**
     * 初始化缓存
     */
    public void init() throws Exception {
        // 获取所有提供者
        ListView<String> listView =  namingService.getServicesOfServer(0, 65535);
        List<String> providers = listView.getData();
        for (String providerName : providers) {
            // 获取健康实例
            List<Instance> instances = namingService.selectInstances(providerName, true);
            for (Instance instance : instances) {
                String ip = instance.getIp();
                int port = instance.getPort();
                // 获取元数据
                Map<String, String> metadata = instance.getMetadata();
                // 获取接口信息
                JSONArray array = JSON.parseArray(metadata.get(CommonConstants.SERVICES));
                // 解析接口信息并缓存
                for (int i = 0;i < array.size();i++) {
                    JSONObject object = array.getJSONObject(i);
                    String interfaceName = object.getString(CommonConstants.INTERFACE);
                    String group = object.getString(CommonConstants.GROUP);
                    List<RemoteService> list = null;
                    if (CACHE.containsKey(interfaceName)) {
                        list = CACHE.get(interfaceName);
                    } else {
                        list = new ArrayList<>();
                    }
                    RemoteService remoteService = new RemoteService();
                    remoteService.setProviderName(providerName);
                    remoteService.setIp(ip);
                    remoteService.setPort(port);
                    remoteService.setGroup(group);
                    list.add(remoteService);
                    CACHE.put(interfaceName, list);
                }
            }
            // 注册监听
            namingService.subscribe(providerName, new EventListener() {
                @Override
                public void onEvent(Event event) {
                    if (event instanceof NamingEvent) {
                        List<Instance> list = ((NamingEvent) event).getInstances();
                        NettyClientCache.updateCache(providerName, list, nettyRpcProperties);
                    }
                }
            });
        }
    }

    /**
     * 获取提供者列表
     * @param interfaceName 远程调用接口名
     * @param providerName 提供者名称
     * @param group 接口组
     * @return 列表
     */
    public List<RemoteService> getServices(String interfaceName, String providerName, String group) {
        if (!CACHE.containsKey(interfaceName)) {
            return null;
        } else {
            List<RemoteService> list = CACHE.get(interfaceName);
            return RemoteServiceUtil.selectRemoteService(list, providerName, group);
        }
    }
}
