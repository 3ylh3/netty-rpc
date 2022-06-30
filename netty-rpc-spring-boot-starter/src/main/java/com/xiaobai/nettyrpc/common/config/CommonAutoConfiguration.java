package com.xiaobai.nettyrpc.common.config;

import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.xiaobai.nettyrpc.common.constants.CommonConstants;
import com.xiaobai.nettyrpc.common.properties.NettyRpcProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 自动装配类
 *
 * @author yinzhaojing
 * @date 2022-06-30 16:45:51
 */
@Configuration
@EnableConfigurationProperties(NettyRpcProperties.class)
public class CommonAutoConfiguration {
    @Autowired
    private NettyRpcProperties nettyRpcProperties;

    @Bean("namingService")
    @ConditionalOnMissingBean(NamingService.class)
    public NamingService initNamingService() throws Exception {
        if (!StringUtils.isBlank(nettyRpcProperties.getNacosAddress())) {
            Properties properties = new Properties();
            properties.setProperty(CommonConstants.SERVER_ADDR, nettyRpcProperties.getNacosAddress());
            properties.setProperty(CommonConstants.NAMESPACE, nettyRpcProperties.getNamespaceId());
            return NamingFactory.createNamingService(properties);
        } else {
            return null;
        }
    }
}
