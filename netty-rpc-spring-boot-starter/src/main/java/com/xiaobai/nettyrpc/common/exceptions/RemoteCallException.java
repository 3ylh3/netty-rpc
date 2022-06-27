package com.xiaobai.nettyrpc.common.exceptions;

/**
 * 远程调用异常
 *
 * @author yinzhaojing
 * @date 2022-06-24 14:08:44
 */
public class RemoteCallException extends RuntimeException {

    public RemoteCallException(String msg) {
        super(msg);
    }
}
