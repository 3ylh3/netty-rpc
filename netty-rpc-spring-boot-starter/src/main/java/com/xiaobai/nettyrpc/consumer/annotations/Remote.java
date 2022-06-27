package com.xiaobai.nettyrpc.consumer.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Remote {
    /**
     * 提供者名称
     */
    String providerName() default "";
    /**
     * 提供者组
     */
    String group() default "";
    /**
     * 提供者地址
     */
    String[] providerAddresses() default {};
}
