package com.xiaobai.nettyrpc.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 传输DTO
 *
 * @author yinzhaojing
 * @date 2022-06-22 19:55:17
 */
@Data
public class TransferDTO implements Serializable {
    private static final long serialVersionUID = -6879139209064867062L;
    /**
     * 请求id
     */
    private String requestId;
    /**
     * 消费者名称
     */
    private String consumerName;
    /**
     * 接口全限定类名
     */
    private String interfaceName;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 请求参数类型
     */
    private Class<?>[] parameterTypes;
    /**
     * 请求参数
     */
    private Object[] params;
    /**
     * 返回码
     */
    private Integer responseCode;
    /**
     * 返回信息
     */
    private String responseMessage;
    /**
     * 远程服务端地址
     */
    private String remoteAddress;
    /**
     * 提供者名称
     */
    private String providerName;
    /**
     * 服务组
     */
    private String serviceGroup;
    /**
     * 方法调用结果
     */
    private Object result;

    /**
     * 拷贝请求信息
     * @param transferDTO 请求DTO
     */
    public void copyRequestValue(TransferDTO transferDTO) {
        this.requestId = transferDTO.getRequestId();
        this.consumerName = transferDTO.getConsumerName();
        this.interfaceName = transferDTO.getInterfaceName();
        this.methodName = transferDTO.getMethodName();
        this.parameterTypes = transferDTO.getParameterTypes();
        this.params = transferDTO.getParams();
    }
}
