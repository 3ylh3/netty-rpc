package com.xiaobai.nettyrpc.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * SPI工具类
 *
 * @author yinzhaojing
 * @date 2022-06-22 15:53:51
 */
public class SPIUtil {

    /**
     * 获取对象
     * @param name 全限定类名
     * @param tClass 类型
     * @return 对象实例
     * @param <T>
     */
    public static <T> T getObject(String name, Class<T> tClass) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(tClass);
        for (T t : serviceLoader) {
            if (StringUtils.equals(name, t.getClass().getName())) {
                return t;
            }
        }
        return null;
    }

    /**
     * 获取对象列表
     * @param list 全限定类名列表
     * @param tClass 类型
     * @return 对象列表
     * @param <T>
     */
    public static <T> List<T> getObjects(List<String> list, Class<T> tClass) {
        List<T> result = new ArrayList<>();
        ServiceLoader<T> serviceLoader = ServiceLoader.load(tClass);
        for (String name : list) {
            for (T t : serviceLoader) {
                if (StringUtils.equals(name, t.getClass().getName())) {
                    result.add(t);
                    break;
                }
            }
        }
        return result;
    }
}
