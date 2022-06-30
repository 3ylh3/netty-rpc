package com.xiaobai.nettyrpc.common.utils;

import com.xiaobai.nettyrpc.common.constants.CommonConstants;
import com.xiaobai.nettyrpc.consumer.config.RemoteService;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * remoteService工具类
 *
 * @author yinzhaojing
 * @date 2022-06-30 19:24:25
 */
public class RemoteServiceUtil {

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
            services.add(remoteService);
        }
        return services;
    }
}
