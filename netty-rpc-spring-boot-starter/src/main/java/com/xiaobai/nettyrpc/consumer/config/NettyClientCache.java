package com.xiaobai.nettyrpc.consumer.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.xiaobai.nettyrpc.common.codec.AbstractDecoder;
import com.xiaobai.nettyrpc.common.codec.AbstractEncoder;
import com.xiaobai.nettyrpc.common.codec.HessianDecoder;
import com.xiaobai.nettyrpc.common.codec.HessianEncoder;
import com.xiaobai.nettyrpc.common.constants.CommonConstants;
import com.xiaobai.nettyrpc.common.properties.NettyRpcProperties;
import com.xiaobai.nettyrpc.common.utils.RemoteServiceUtil;
import com.xiaobai.nettyrpc.common.utils.SPIUtil;
import com.xiaobai.nettyrpc.common.entity.RemoteService;
import com.xiaobai.nettyrpc.common.loadbalancer.service.Loadbalancer;
import com.xiaobai.nettyrpc.common.loadbalancer.service.impl.RandomLoadbalancer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * netty客户端缓存
 *
 * @author yinzhaojing
 * @date 2022-06-22 20:21:33
 */
public class NettyClientCache {
    private static final Logger logger = LoggerFactory.getLogger(NettyClientCache.class);
    /**
     * 缓存远程调用接口全限定类名对应远程服务服务端信息
     */
    private static final Map<String, List<RemoteService>> INTERFACE_ADDRESS_MAP = new ConcurrentHashMap<>();
    /**
     * 缓存远程服务端地址对应netty客户端
     */
    private static final Map<String, NettyClient> NETTY_CLIENT_MAP = new ConcurrentHashMap<>();

    /**
     * 添加缓存
     * @param key 缓存key
     * @param nettyRpcProperties 配置项
     */
    public static void add(String key, List<RemoteService> list,
                    NettyRpcProperties nettyRpcProperties) throws Exception {
        logger.info("start add netty client cache...");
        synchronized (INTERFACE_ADDRESS_MAP) {
            if (INTERFACE_ADDRESS_MAP.containsKey(key)) {
                return;
            }
            INTERFACE_ADDRESS_MAP.put(key, list);
            Integer timeout = null == nettyRpcProperties.getTimeout() ? CommonConstants.DEFAULT_TIMEOUT
                    : nettyRpcProperties.getTimeout();
            // 初始化client
            initNettyClient(list, timeout, nettyRpcProperties.getEncodeClassName(),
                    nettyRpcProperties.getDecodeClassName(), nettyRpcProperties.getCompression());
        }
        logger.info("add netty client success");
    }

    /**
     * 从缓存中获取netty client
     * @param key 缓存key
     * @param providerName 提供者名称
     * @param group 接口组
     * @param loadbalancerClass 负载均衡实现类全限定类名
     * @return client
     */
    public static NettyClient getClient(String key, String providerName, String group, String loadbalancerClass) {
        if (INTERFACE_ADDRESS_MAP.containsKey(key)) {
            List<RemoteService> list = INTERFACE_ADDRESS_MAP.get(key);
            // 根据providerName和group筛选
            List<RemoteService> services = RemoteServiceUtil.selectRemoteServiceDistinct(list, providerName, group);
            if (services.isEmpty()) {
                logger.error("no remote service find");
                return null;
            }
            // 根据负载均衡策略选取一个远程服务
            Loadbalancer loadbalancer = SPIUtil.getObject(loadbalancerClass, Loadbalancer.class);
            if (null == loadbalancer) {
                // 默认随机
                loadbalancer = new RandomLoadbalancer();
            }
            RemoteService remoteService = loadbalancer.selectRemoteService(providerName, key, group, services);
            if (null == remoteService) {
                logger.error("no remote service find");
                return null;
            }
            // 获取对应的netty client
            String clientCacheKey = remoteService.getIp() + CommonConstants.ADDRESS_DELIMITER + remoteService.getPort();
            if (NETTY_CLIENT_MAP.containsKey(clientCacheKey)) {
                return NETTY_CLIENT_MAP.get(clientCacheKey);
            } else {
                logger.error("no client find,remote ip:{},remote port:{}", remoteService.getIp(),
                        remoteService.getPort());
                return null;
            }
        } else {
            logger.error("no client find,key:{}", key);
            return null;
        }
    }

    /**
     * 获取远程调用接口全限定名对应远程服务地址缓存的entry set
     * @return entry set
     */
    public static Set<Map.Entry<String, List<RemoteService>>> getInterfaceSet() {
        return INTERFACE_ADDRESS_MAP.entrySet();
    }

    /**
     * 根据ip端口获取netty客户端
     * @param address ip端口
     * @return netty客户端
     */
    public static NettyClient getClientByAddress(String address) {
        return NETTY_CLIENT_MAP.get(address);
    }

    /**
     * 和远程服务端建立长连接，并缓存client
     * @param list 远程服务信息列表
     * @param timeout 超时时间
     * @param encodeClassName 自定义编码类全限定类名
     * @param decodeClassName 自定义解码类全限定类名
     * @param isCompression 是否开启数据压缩
     */
    private static void initNettyClient(List<RemoteService> list, Integer timeout, String encodeClassName,
                                        String decodeClassName, Boolean isCompression) throws Exception {
        for (RemoteService remoteService : list) {
            String key = remoteService.getIp() + CommonConstants.ADDRESS_DELIMITER + remoteService.getPort();
            // 判断缓存中是否已经有对应的channel
            if (NETTY_CLIENT_MAP.containsKey(key)) {
                continue;
            }
            logger.info("start init netty client,remote server ip:{},port:{}...", remoteService.getIp(),
                    remoteService.getPort());
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel socketChannel) {
                                // 使用\r\n分隔消息
                                socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE,
                                        Delimiters.lineDelimiter()[0]));
                                // SPI获取序列化对象，默认使用Hessian序列化方式
                                AbstractDecoder decoder = SPIUtil.getObject(decodeClassName, AbstractDecoder.class);
                                if (null == decoder) {
                                    decoder = new HessianDecoder();
                                }
                                if (null != isCompression && isCompression) {
                                    decoder.setCompression(true);
                                }
                                socketChannel.pipeline().addLast(decoder);
                                socketChannel.pipeline().addLast(new ClientHandler());
                                AbstractEncoder encoder = SPIUtil.getObject(encodeClassName, AbstractEncoder.class);
                                if (null == encoder) {
                                    encoder = new HessianEncoder();
                                }
                                if (null != isCompression && isCompression) {
                                    encoder.setCompression(true);
                                }
                                socketChannel.pipeline().addLast(encoder);
                            }
                        });
                ChannelFuture channelFuture = bootstrap.connect(remoteService.getIp(), remoteService.getPort())
                        .sync();
                NettyClient nettyClient = new NettyClient(timeout, channelFuture);
                // 缓存client
                NETTY_CLIENT_MAP.put(key, nettyClient);
                logger.info("init netty client success");
            } catch (Exception e) {
                logger.error("init netty client exception:", e);
                throw e;
            }
        }
    }

    /**
     * 更新缓存
     * @param providerName 提供者名称
     * @param list 实例列表
     * @param nettyRpcProperties 配置类
     */
    public static void updateCache(String providerName, List<Instance> list,
                                   NettyRpcProperties nettyRpcProperties) {
        Set<String> set = new HashSet<>();
        for (Instance instance : list) {
            set.add(instance.getIp() + CommonConstants.ADDRESS_DELIMITER + instance.getPort());
        }
        Set<String> deleteSet = new HashSet<>();
        synchronized (INTERFACE_ADDRESS_MAP) {
            for (Map.Entry<String, List<RemoteService>> entry : INTERFACE_ADDRESS_MAP.entrySet()) {
                String key = entry.getKey();
                if (StringUtils.contains(key, CommonConstants.CACHE_KEY_DELIMITER)) {
                    // 含有|，表示此client缓存信息是从Remote注解中指定的地址创建的，无需更新
                    continue;
                }
                List<RemoteService> remoteServices = entry.getValue();
                // 记录需要删除的client
                for (RemoteService remoteService : remoteServices) {
                    if (!StringUtils.equals(providerName, remoteService.getProviderName())) {
                        continue;
                    }
                    String address = remoteService.getIp() + CommonConstants.ADDRESS_DELIMITER
                            + remoteService.getPort();
                    if (!set.contains(address)) {
                        deleteSet.add(address);
                    }
                }
                List<RemoteService> newRemoteServices = getNewRemoteServices(key, providerName, list);
                // 更新缓存
                INTERFACE_ADDRESS_MAP.put(key, newRemoteServices);
                Integer timeout = null == nettyRpcProperties.getTimeout() ? CommonConstants.DEFAULT_TIMEOUT
                        : nettyRpcProperties.getTimeout();
                try {
                    initNettyClient(newRemoteServices, timeout, nettyRpcProperties.getEncodeClassName(),
                            nettyRpcProperties.getDecodeClassName(), nettyRpcProperties.getCompression());
                } catch (Exception e) {
                    logger.error("update cache exception,provider name:{}", providerName, e);
                }
            }
            // 关掉旧的netty客户端
            shutDownClient(deleteSet);
        }
    }

    /**
     * 获取新的远程服务列表
     * @param interfaceName 接口名
     * @param providerName 提供者名
     * @param list 新实例列表
     * @return
     */
    private static List<RemoteService> getNewRemoteServices(String interfaceName, String providerName,
                                                            List<Instance> list) {
        List<RemoteService> remoteServices = new ArrayList<>();
        for (Instance instance : list) {
            if (!instance.isHealthy()) {
                continue;
            }
            Map<String, String> metadata = instance.getMetadata();
            JSONArray array = JSON.parseArray(metadata.get(CommonConstants.SERVICES));
            // 解析元数据
            for (int i = 0;i < array.size();i++) {
                JSONObject object = array.getJSONObject(i);
                if (StringUtils.equals(interfaceName, object.getString(CommonConstants.INTERFACE))) {
                    // 匹配接口名
                    RemoteService remoteService = new RemoteService();
                    remoteService.setProviderName(providerName);
                    remoteService.setIp(instance.getIp());
                    remoteService.setPort(instance.getPort());
                    remoteService.setGroup(object.getString(CommonConstants.GROUP));
                    remoteService.setWeight(object.getInteger(CommonConstants.WEIGHT));
                    remoteService.setIsHealthy(true);
                    remoteServices.add(remoteService);
                }
            }
        }
        return remoteServices;
    }

    /**
     * 关掉netty客户端
     * @param set 地址列表
     */
    private static void shutDownClient(Set<String> set) {
        for (String address : set) {
            NettyClient nettyClient = NETTY_CLIENT_MAP.remove(address);
            nettyClient.close();
        }
    }
}
