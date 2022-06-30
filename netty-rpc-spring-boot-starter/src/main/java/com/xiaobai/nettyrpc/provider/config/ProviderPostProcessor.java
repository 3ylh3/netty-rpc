package com.xiaobai.nettyrpc.provider.config;

import com.xiaobai.nettyrpc.common.constants.CommonConstants;
import com.xiaobai.nettyrpc.common.properties.NettyRpcProperties;
import com.xiaobai.nettyrpc.provider.annotations.Service;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.annotation.Annotation;

/**
 * provider bean前置处理
 *
 * @author yinzhaojing
 * @date 2022-06-24 15:01:27
 */
public class ProviderPostProcessor implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ProviderPostProcessor.class);

    @Autowired
    private NettyRpcProperties nettyRpcProperties;
    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * 解析@Service注解，缓存接口实现类并将服务注册到注册中心
     * @param bean bean
     * @param beanName bean名称
     * @return bean
     * @throws BeansException 异常
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        Annotation[] annotations = clazz.getAnnotations();
        for(Annotation annotation : annotations) {
            //如果有@Service注解修饰的bean，则进行缓存，待所有bean加载完毕后向注册中心中注册
            if (annotation instanceof Service) {
                logger.info("find provider service:{}", clazz.getName());
                Class<?>[] interfaces = clazz.getInterfaces();
                for(Class<?> interfaceClazz : interfaces) {
                    ProviderService providerService = new ProviderService();
                    providerService.setInterfaceName(interfaceClazz.getName());
                    providerService.setImplName(clazz.getName());
                    String group = ((Service) annotation).group();
                    providerService.setGroup(group);
                    String providerName = StringUtils.isBlank(nettyRpcProperties.getName()) ? applicationName
                            : nettyRpcProperties.getName();
                    if (StringUtils.isBlank(providerName)) {
                        throw new BeanCreationException("provider name is null!");
                    }
                    providerService.setProviderName(providerName);
                    ProviderServiceCache.add(interfaceClazz.getName() + CommonConstants.CACHE_KEY_DELIMITER
                            + group, providerService);
                }
            }
        }
        return bean;
    }
}
