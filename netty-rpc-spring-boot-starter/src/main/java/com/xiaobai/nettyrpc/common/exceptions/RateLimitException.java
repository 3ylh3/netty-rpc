package com.xiaobai.nettyrpc.common.exceptions;

/**
 * 限流异常
 *
 * @author yinzhaojing
 * @date 2022-07-20 19:41:52
 */
public class RateLimitException extends RuntimeException {

    public RateLimitException(String msg) {
        super(msg);
    }
}
