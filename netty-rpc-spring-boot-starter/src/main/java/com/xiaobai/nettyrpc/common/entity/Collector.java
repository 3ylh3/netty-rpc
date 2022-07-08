package com.xiaobai.nettyrpc.common.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 指标数据集合
 *
 * @author yinzhaojing
 * @date 2022-07-08 16:21:18
 */
public class Collector {
    private Map<String, Object> map;

    public Collector() {
        this.map = new ConcurrentHashMap<>();
    }

    public Object get(String name) {
        return this.map.get(name);
    }

    public void set(String name, Object object) {
        this.map.put(name, object);
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }
}