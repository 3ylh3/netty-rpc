package com.xiaobai.nettyrpc.consumer.config;

import lombok.Data;

/**
 * 远程服务端实体类
 *
 * @author yinzhaojing
 * @date 2022-06-23 15:01:50
 */
@Data
public class RemoteService {
    /**
     * ip地址
     */
    private String ip;
    /**
     * 端口
     */
    private Integer port;
}
