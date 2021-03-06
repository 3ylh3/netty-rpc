package com.xiaobai.nettyrpc.common.utils;

import com.xiaobai.nettyrpc.common.constants.CommonConstants;
import com.xiaobai.nettyrpc.common.entity.RemoteService;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * remoteService工具类
 *
 * @author yinzhaojing
 * @date 2022-06-30 19:24:25
 */
public class RemoteServiceUtil {

    /**
     * 根据提供者名称和接口组筛选remoteService,按照providerName group和ip端口去重
     * @param list remoteService列表
     * @param providerName 提供者名
     * @param group 接口组
     * @return 结果
     */
    public static List<RemoteService> selectRemoteServiceDistinct(List<RemoteService> list, String providerName,
                                                          String group) {
        List<RemoteService> services = new ArrayList<>();
        // 滤重用
        Set<String> set = new HashSet<>();
        for (RemoteService remoteService : list) {
            if (!StringUtils.equals(CommonConstants.DEFAULT, providerName)
                    && !StringUtils.equals(providerName, remoteService.getProviderName())) {
                continue;
            }
            if (!StringUtils.equals(CommonConstants.DEFAULT, group)
                    && !StringUtils.equals(group, remoteService.getGroup())) {
                continue;
            }
            RemoteService tmp = new RemoteService();
            tmp.copyFrom(remoteService);
            if (StringUtils.equals(CommonConstants.DEFAULT, providerName)) {
                tmp.setProviderName(CommonConstants.DEFAULT);
            }
            if (StringUtils.equals(CommonConstants.DEFAULT, group)) {
                tmp.setGroup(CommonConstants.DEFAULT);
            }
            String key = remoteService.getProviderName() + remoteService.getGroup() + remoteService.getIp()
                    + remoteService.getPort();
            if (!set.contains(key) && tmp.getIsHealthy()) {
                services.add(tmp);
                set.add(key);
            }
        }
        return services;
    }

    /**
     * 根据提供者名称和服务组筛选remoteService
     * @param list remoteService列表
     * @param providerName 提供者名
     * @param group 服务组
     * @return 结果
     */
    public static List<RemoteService> selectRemoteService(List<RemoteService> list, String providerName,
                                                          String group) {
        List<RemoteService> services = new ArrayList<>();
        for (RemoteService remoteService : list) {
            if (!StringUtils.equals(CommonConstants.DEFAULT, providerName)
                    && !StringUtils.equals(providerName, remoteService.getProviderName())) {
                continue;
            }
            if (!StringUtils.equals(CommonConstants.DEFAULT, group)
                    && !StringUtils.equals(group, remoteService.getGroup())) {
                continue;
            }
            if (!remoteService.getIsHealthy()) {
                continue;
            }
            services.add(remoteService);
        }
        return services;
    }
}
