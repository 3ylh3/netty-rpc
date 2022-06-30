package com.xiaobai.nettyrpc.consumer.config;

import com.xiaobai.nettyrpc.codec.AbstractDecoder;
import com.xiaobai.nettyrpc.codec.AbstractEncoder;
import com.xiaobai.nettyrpc.codec.HessianDecoder;
import com.xiaobai.nettyrpc.codec.HessianEncoder;
import com.xiaobai.nettyrpc.common.constants.CommonConstants;
import com.xiaobai.nettyrpc.common.properties.NettyRpcProperties;
import com.xiaobai.nettyrpc.common.utils.RemoteServiceUtil;
import com.xiaobai.nettyrpc.common.utils.SPIUtil;
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
                    nettyRpcProperties.getDecodeClassName());
        }
        logger.info("add netty client success");
    }

    /**
     * 从缓存中获取netty client
     * @param key 缓存key
     * @param providerName 提供者名称
     * @param group 服务组
     * @return client
     */
    public static NettyClient getClient(String key, String providerName, String group) {
        if (INTERFACE_ADDRESS_MAP.containsKey(key)) {
            List<RemoteService> list = INTERFACE_ADDRESS_MAP.get(key);
            // 根据providerName和group筛选
            List<RemoteService> services = RemoteServiceUtil.selectRemoteService(list, providerName, group);


            // TODO 根据负载均衡策略选取一个远程服务
            RemoteService remoteService = services.get(0);


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
     */
    private static void initNettyClient(List<RemoteService> list, Integer timeout, String encodeClassName,
                                        String decodeClassName) throws Exception {
        for (RemoteService remoteService : list) {
            String key = remoteService.getIp() + CommonConstants.ADDRESS_DELIMITER + remoteService.getPort();
            // 判断缓存中是否已经有对应的channel
            if (NETTY_CLIENT_MAP.containsKey(key)) {
                return;
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
                                socketChannel.pipeline().addLast(decoder);
                                socketChannel.pipeline().addLast(new ClientHandler());
                                AbstractEncoder encoder = SPIUtil.getObject(encodeClassName, AbstractEncoder.class);
                                if (null == encoder) {
                                    encoder = new HessianEncoder();
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
}
