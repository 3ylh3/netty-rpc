package com.xiaobai.nettyrpc.common.constants;

/**
 * 公共常量类
 *
 * @author yinzhaojing
 * @date 2022-06-23 19:45:46
 */
public class CommonConstants {
    public static final String ADDRESS_DELIMITER = ":";
    public static final String CACHE_KEY_DELIMITER = "|";
    public static final int SUCCESS_CODE = 0;
    public static final int ERROR_CODE = 1;
    public static final int TIMEOUT_CODE = 2;
    public static final String TIMEOUT_MESSAGE = "time out";
    public static final String SUCCESS_MESSAGE = "success";
    public static final String LINE_DELIMITER = "\r\n";
    public static final Integer DEFAULT_PORT = 18317;
    public static final Integer DEFAULT_TIMEOUT = 60;
    public static final String HEART_BEAT = "heart beat";
    public static final Integer DEFAULT_PROVIDER_CORE_POOL_SIZE = 200;
    public static final Integer DEFAULT_PROVIDER_MAX_POOL_SIZE = 500;
    public static final Integer DEFAULT_PROVIDER_QUEUE_CAPACITY = 500;
    public static final Integer DEFAULT_PROVIDER_KEEP_ALIVE_SECONDS = 10;
    public static final String PROVIDER_PROCESS_THREAD_NAME_PREFIX = "provider-process";
    public static final String SERVER_ADDR = "serverAddr";
    public static final String NAMESPACE = "namespace";
    public static final String DEFAULT = "default";
    public static final String INTERFACE = "interface";
    public static final String GROUP = "group";
    public static final String IMPL = "impl";
    public static final String SERVICES = "services";
    public static final String WEIGHT = "weight";
    public static final String METRIC_FILE = "metrics.json";
    public static final String UTF8 = "UTF-8";
    public static final String COUNTER = "Counter";
    public static final String GAUGE = "Gauge";
    public static final String HISTOGRAM = "Histogram";
    public static final String SUMMARY = "Summary";
    public static final String FAIL = "fail";
    public static final String SUCCESS = "success";
    public static final String RATE_LIMIT = "rate limit";
    public static final String TIME_TICK_THREAD = "time-tick-thread";
    public static final String PROMETHEUS = "prometheus";
    public static final String RATE_LIMIT_CAPACITY = "rate_limit_capacity";
    public static final String RATE_LIMIT_RATE = "rate_limit_rate";
}
