package com.xiaobai.nettyrpc.common.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.xiaobai.nettyrpc.common.constants.CommonConstants;
import com.xiaobai.nettyrpc.common.entity.Collector;
import com.xiaobai.nettyrpc.common.entity.MetricInfo;
import com.xiaobai.nettyrpc.common.properties.NettyRpcProperties;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

/**
 * 自动装配类
 *
 * @author yinzhaojing
 * @date 2022-06-30 16:45:51
 */
@Configuration
@EnableConfigurationProperties(NettyRpcProperties.class)
public class CommonAutoConfiguration {
    @Autowired
    private NettyRpcProperties nettyRpcProperties;
    @Autowired
    private PrometheusMeterRegistry registry;
    @Value("${management.endpoints.web.exposure.include:null}")
    private String enablePrometheus;

    @Bean("namingService")
    @ConditionalOnMissingBean(NamingService.class)
    public NamingService initNamingService() throws Exception {
        if (!StringUtils.isBlank(nettyRpcProperties.getNacosAddress())) {
            Properties properties = new Properties();
            properties.setProperty(CommonConstants.SERVER_ADDR, nettyRpcProperties.getNacosAddress());
            if (!StringUtils.isBlank(nettyRpcProperties.getNamespaceId())) {
                properties.setProperty(CommonConstants.NAMESPACE, nettyRpcProperties.getNamespaceId());
            }
            return NamingFactory.createNamingService(properties);
        } else {
            return null;
        }
    }

    @Bean("collector")
    @ConditionalOnMissingBean(Collector.class)
    public Collector initCollector() throws Exception {
        Collector collector = new Collector();
        if (StringUtils.equals(enablePrometheus, CommonConstants.PROMETHEUS)) {
            //解析json文件，根据指标列表创建对应类型的prometheus collector并注册
            Resource resource = new ClassPathResource(CommonConstants.METRIC_FILE);
            InputStream inputStream = resource.getInputStream();
            StringWriter stringWriter = new StringWriter();
            IOUtils.copy(inputStream, stringWriter, CommonConstants.UTF8);
            List<MetricInfo> list = JSON.parseArray(stringWriter.toString(), MetricInfo.class);
            for(MetricInfo metricInfo : list) {
                switch (metricInfo.getType()) {
                    case CommonConstants.COUNTER:
                        collector.set(metricInfo.getName(), Counter.build().name(metricInfo.getName())
                                .help(metricInfo.getDescription()).labelNames(metricInfo.getLabels())
                                .register(registry.getPrometheusRegistry()));
                        break;
                    case CommonConstants.GAUGE:
                        collector.set(metricInfo.getName(), Gauge.build().name(metricInfo.getName())
                                .help(metricInfo.getDescription()).labelNames(metricInfo.getLabels())
                                .register(registry.getPrometheusRegistry()));
                        break;
                    case CommonConstants.HISTOGRAM:
                        collector.set(metricInfo.getName(), Histogram.build().name(metricInfo.getName())
                                .help(metricInfo.getDescription()).labelNames(metricInfo.getLabels())
                                .buckets(metricInfo.getBuckets()).register(registry.getPrometheusRegistry()));
                        break;
                    case CommonConstants.SUMMARY:
                        collector.set(metricInfo.getName(), Summary.build().name(metricInfo.getName())
                                .help(metricInfo.getDescription()).labelNames(metricInfo.getLabels())
                                .register(registry.getPrometheusRegistry()));
                        break;
                    default:
                }
            }
        }
        return collector;
    }
}
