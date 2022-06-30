package com.xiaobai.nettyrpc.consumer.config;

import com.xiaobai.nettyrpc.common.constants.CommonConstants;
import com.xiaobai.nettyrpc.common.exceptions.RemoteCallException;
import com.xiaobai.nettyrpc.common.properties.NettyRpcProperties;
import com.xiaobai.nettyrpc.consumer.annotations.Remote;
import com.xiaobai.nettyrpc.dto.TransferDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * consumer bean前置处理
 *
 * @author yinzhaojing
 * @date 2022-06-24 10:10:39
 */
public class ConsumerPostProcessor implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerPostProcessor.class);

    @Value("${spring.application.name}")
    private String applicationName;
    @Autowired
    private NettyRpcProperties nettyRpcProperties;
    @Autowired
    private RegistryCache registryCache;

    /**
     * 解析@Remote注解，获取接口全限定类名并从注册中心获取远程server端地址，建立netty长连接并缓存，使用cglib动态代理生成调用对象
     * @param bean bean
     * @param beanName bean名称
     * @return 动态代理对象
     * @throws BeansException 异常
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        try {
            Class<?> clazz = bean.getClass();
            Field[] fields = clazz.getDeclaredFields();
            Enhancer enhancer = new Enhancer();
            for(Field field : fields) {
                //如果有@Remote注解修饰的变量，则解析注解，获得远程服务端地址
                Remote remote = field.getAnnotation(Remote.class);
                String interfaceName = field.getType().getName();
                if (null != remote) {
                    logger.info("start init bean:{},remote call interface:{}...", beanName, interfaceName);
                    String providerName = remote.providerName();
                    String group = remote.group();
                    List<String> providerAddresses = Arrays.asList(remote.providerAddresses());
                    List<RemoteService> remoteServices = new ArrayList<>();
                    String key = null;
                    if (providerAddresses.isEmpty()) {
                        key = interfaceName;
                        // 注解中未指定服务端地址，从注册中心缓存中查询
                        remoteServices = registryCache.getServices(interfaceName, providerName, group);
                        if (null == remoteServices || remoteServices.isEmpty()) {
                            logger.error("not find remote service in registry center");
                            throw new BeanCreationException("not find remote service in registry center");
                        }
                    } else {
                        // 缓存key添加bean name和注册中心的服务做区分
                        key = beanName + CommonConstants.CACHE_KEY_DELIMITER + interfaceName;
                        for (String providerAddress : providerAddresses) {
                            String serviceIp = providerAddress.split(CommonConstants.ADDRESS_DELIMITER)[0];
                            Integer servicePort = Integer.parseInt(
                                    providerAddress.split(CommonConstants.ADDRESS_DELIMITER)[1]);
                            RemoteService remoteService = new RemoteService();
                            remoteService.setProviderName(remote.providerName());
                            remoteService.setGroup(remote.group());
                            remoteService.setIp(serviceIp);
                            remoteService.setPort(servicePort);
                            remoteServices.add(remoteService);
                        }
                    }
                    // 初始化netty客户端并缓存
                    NettyClientCache.add(key, remoteServices, nettyRpcProperties);
                    // 动态代理生成远程调用对象
                    generateObject(bean, enhancer, field, remote, key, providerName, group);
                    logger.info("init success");
                }
            }
        } catch (Exception e) {
            logger.error("init exception:", e);
            throw new BeanCreationException(e.getMessage());
        }

        return bean;
    }

    /**
     * cglib动态代理生成远程调用对象
     * @param bean bean
     * @param enhancer enhancer
     * @param field 远程调用接口
     * @param remote remote注解
     * @param key 缓存key
     * @param providerName 提供者名
     * @param group 服务组名
     * @throws Exception 异常
     */
    private void generateObject(Object bean, Enhancer enhancer, Field field, Remote remote, String key,
                                String providerName, String group) throws Exception {
        enhancer.setSuperclass(field.getType());
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object object, Method method, Object[] objects,
                                    MethodProxy methodProxy) throws Throwable {
                String requestId = UUID.randomUUID().toString();
                logger.info("start call remote service:{},request id:{}", field.getType().getName()
                        + CommonConstants.ADDRESS_DELIMITER + method.getName(), requestId);
                // 从netty client缓存中获取client
                NettyClient nettyClient = NettyClientCache.getClient(key, providerName, group);
                if (null == nettyClient) {
                    throw new RemoteCallException("no provider find");
                }
                // 构造请求对象
                TransferDTO requestDTO = new TransferDTO();
                requestDTO.setRequestId(requestId);
                String consumerName = StringUtils.isBlank(nettyRpcProperties.getName()) ? applicationName
                        : nettyRpcProperties.getName();
                requestDTO.setConsumerName(consumerName);
                requestDTO.setInterfaceName(field.getType().getName());
                requestDTO.setMethodName(method.getName());
                requestDTO.setParameterTypes(method.getParameterTypes());
                requestDTO.setParams(objects);
                requestDTO.setProviderName(remote.providerName());
                requestDTO.setServiceGroup(remote.group());


                // TODO 使用SPI机制加载配置文件中指定的处理链做前置处理


                // 发送请求
                TransferDTO responseDTO = nettyClient.send(requestDTO);
                if (CommonConstants.ERROR_CODE == responseDTO.getResponseCode()) {
                    logger.error("call remote service error:{}", responseDTO.getResponseCode());
                    throw new RemoteCallException(responseDTO.getResponseMessage());
                } else if (CommonConstants.TIMEOUT_CODE == responseDTO.getResponseCode()) {
                    logger.error("call remote service timeout");
                    throw new RemoteCallException(responseDTO.getResponseMessage());
                }


                // TODO 使用SPI机制加载配置文件中指定的处理链做前置处理


                logger.info("call remote service success,provider name:{}, remote service address:{}",
                        responseDTO.getProviderName(), responseDTO.getRemoteAddress());
                return responseDTO.getResult();
            }
        });
        field.setAccessible(true);
        field.set(bean, enhancer.create());
    }
}
