package com.xiaobai.nettyrpc.consumer.heartbeat;

import com.xiaobai.nettyrpc.common.constants.CommonConstants;
import com.xiaobai.nettyrpc.common.properties.NettyRpcProperties;
import com.xiaobai.nettyrpc.consumer.config.NettyClient;
import com.xiaobai.nettyrpc.consumer.config.NettyClientCache;
import com.xiaobai.nettyrpc.consumer.config.RemoteService;
import com.xiaobai.nettyrpc.dto.TransferDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 客户端心跳检测
 *
 * @author yinzhaojing
 * @date 2022-06-27 15:59:13
 */
public class ClientHeartBeat {
    private static final Logger logger = LoggerFactory.getLogger(ClientHeartBeat.class);
    // 缓存心跳失败次数
    private final Map<String, Integer> map = new HashMap<>(8);

    @Value("${spring.application.name}")
    private String applicationName;
    @Autowired
    private NettyRpcProperties nettyRpcProperties;

    /**
     * 每个30秒发送心跳，检测与服务端的长链接是否可用
     */
    @Scheduled(cron = "0/30 * * * * ? ")
    public void heartBeat() {
        // 记录检查过的远端地址
        Set<String> checkedAddress = new HashSet<>();
        // 获取远程调用接口全限定名对应服务端地址缓存的entry set
        Set<Map.Entry<String, List<RemoteService>>> interfaceSet = NettyClientCache.getInterfaceSet();
        for (Map.Entry<String, List<RemoteService>> interfaceEntry : interfaceSet) {
            List<RemoteService> remoteServices = interfaceEntry.getValue();
            for (RemoteService remoteService : remoteServices) {
                String ip = remoteService.getIp();
                int port = remoteService.getPort();
                // 是否已经检查过
                String key = ip + CommonConstants.ADDRESS_DELIMITER + port;
                if (checkedAddress.contains(key)) {
                    // 本次已经检查过
                    continue;
                }
                // 记录已经检查过
                checkedAddress.add(key);
                // 获取对应netty客户端
                NettyClient nettyClient = NettyClientCache.getClientByAddress(key);
                if (null == nettyClient) {
                    logger.error("not find remote server:{}", key);

                    // TODO 触发netty客户端缓存更新

                    continue;
                }
                // 发送心跳
                TransferDTO requestDTO = new TransferDTO();
                requestDTO.setRequestId(UUID.randomUUID().toString());
                String consumerName = StringUtils.isBlank(nettyRpcProperties.getName()) ? applicationName
                        : nettyRpcProperties.getName();
                requestDTO.setConsumerName(consumerName);
                requestDTO.setMethodName(CommonConstants.HEART_BEAT);
                logger.info("send heart beat to remote server:{}", key);
                TransferDTO responseDTO = nettyClient.send(requestDTO);
                int count = 0;
                synchronized (map) {
                    if (CommonConstants.SUCCESS_CODE != responseDTO.getResponseCode()) {
                        // 发送心跳失败
                        logger.error("send heart beat error");
                        if (map.containsKey(key)) {
                            count = map.get(key) + 1;
                        } else {
                            count++;
                        }
                        if (5 <= count) {
                            // TODO 连续5次心跳失败,触发netty客户端缓存更新

                            logger.info("缓存更新");


                            // 失败次数清0
                            count = 0;
                        }
                    }
                    // 记录失败次数
                    map.put(key, count);
                }
            }
        }
    }
}