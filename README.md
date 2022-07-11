# netty-rpc
使用Nacos做注册中心，基于Netty通信的RPC框架（springboot工程使用）。
# 1.使用
clone代码，编译后引入jar包：
```xml
<dependency>
    <groupId>com.xiaobai.netty-rpc</groupId>
    <artifactId>netty-rpc-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
## 1.1 Consumer端
### 1.1.1 基础配置
application.properties配置文件中添加如下配置：
```properties
#nacos地址
netty-rpc.nacos-address=127.0.0.1:8848
#nacos namespace id，非必须，默认public namespace
netty-rpc.namespace-id=465d5b72-b6be-40bf-ba02-1cc1db659ef8
```
在远程调用的接口上添加@Remote(com.xiaobai.nettyrpc.consumer.annotations.Remote)注解:
```java
@RestController
public class TestController {
    @Remote
    private Test test;

    @RequestMapping("/test")
    public String test() {
        return test.test("xiaobai");
    }
}
```
### 1.1.2 指定Provider地址调用
@Remote注解中可以指定远程服务的地址，若未指定，则从注册中心中获取：
```java
@Remote(providerAddresses = {"127.0.0.1:18317"})
```
### 1.1.3 指定Provider名称或者实现类group
@Remote注解中可以指定provider名称以及接口组，若未指定，则从所有provider的所有接口实现类中按照负载均衡策略选择：
```java
@Remote(providerName = "test", group = "test")
```
### 1.1.4 指定负载均衡策略
@Remote注解中可以指定负载均衡策略(负载均衡器实现类全限定类名)，若未指定，则使用随机策略:
```java
@Remote(loadbalancer = "com.xiaobai.nettyrpc.common.loadbalancer.service.impl.WeightLoadbalancer")
```
提供三种负载均衡策略：
 - 随机（默认）
 - 轮询（com.xiaobai.nettyrpc.common.loadbalancer.service.impl.PollingLoadbalancer）
 - 随机权重（com.xiaobai.nettyrpc.common.loadbalancer.service.impl.WeightLoadbalancer）

可以自定义负载均衡策略，具体方法如下：  
1.编写自己的负载均衡器，继承com.xiaobai.nettyrpc.common.loadbalancer.service.Loadbalancer并实现selectRemoteService方法，该方法描述如下：
```java
/**
 * 根据负载均衡策略选择一个远程服务实例
 * @param providerName 提供者名称
 * @param interfaceName 远程调用接口名
 * @param group 接口组
 * @param list 远程服务列表
 * @return 远程服务实例
 */
RemoteService selectRemoteService(String providerName, String interfaceName, String group, List<RemoteService> list);
```
2.在resources目录下新建META-INF/services/com.xiaobai.nettyrpc.common.loadbalancer.service.Loadbalancer文件，内容为自定义负载均衡器全限定类名。  
3.在@Remote注解中指定自定义负载均衡器全限定类名。
### 1.1.5 指定超时时间
可在配置文件中指定调用超时时长(s)，默认60s:
```properties
netty-rpc.timeout=10
```
### 1.1.6 指定序列化方法
可在配置文件中指定序列化方式(encode和decode实现类全限定类名)，若未指定，默认为Hessian序列化：
```properties
netty-rpc.encode-class-name=com.xiaobai.nettyrpc.common.codec.JSONEncoder
netty-rpc.decode-class-name=com.xiaobai.nettyrpc.common.codec.JSONDecoder
```
提供两种序列化方式：
 - Hessian(默认)
 - JSON(com.xiaobai.nettyrpc.common.codec.JSONEncoder和com.xiaobai.nettyrpc.common.codec.JSONDecoder)

可以自定义序列化方式，具体方法如下：  
1.编写自定义编码器，继承com.xiaobai.nettyrpc.common.codec.AbstractEncoder类，实现encode方法，该方法描述如下：
```java
/**
 * 编码
 * @param msg 请求信息
 * @return 编码后byte数组
 * @throws Exception 异常
 */
public abstract byte[] encode(TransferDTO msg) throws Exception;
```
2.编写自定义解码器，继承com.xiaobai.nettyrpc.common.codec.AbstractDecoder类，实现decode方法，该方法描述如下：
```java
/**
 * 解码
 * @param bytes byte数组
 * @return 解码后返回对象
 * @throws Exception 异常
 */
public abstract TransferDTO decode(byte[] bytes) throws Exception;
```
3.在resources目录下新建META-INF/services/com.xiaobai.nettyrpc.common.codec.AbstractEncoder文件，内容为自定义编码器全限定类名。  
4.在resources目录下新建META-INF/services/com.xiaobai.nettyrpc.common.codec.AbstractDecoder文件，内容为自定义解码器全限定类名。  
5.在application.properties配置文件中指定自定义编码器和解码器：
```properties
#自定义编码器全限定类名
netty-rpc.encode-class-name=xxx
#自定义解码器全限定类名
netty-rpc.decode-class-name=xxx
```
注：需要注意Consumer和Provider需使用同样的序列化方式，否则会报错。
### 1.1.7 自定义远程调用前置处理和后置处理
netty-rpc框架提供了Consumer远程调用前置处理和后置处理机制，可以自定义前置处理器以及后置处理器，在远程调用之前和之后做处理，具体方法如下：  
1.编写自定义Consumer前置处理器，继承com.xiaobai.nettyrpc.consumer.processor.ConsumerPreProcessor接口，实现doPreProcess方法，该方法描述如下：
```java
/**
 * 前置处理
 * @param requestDTO 请求DTO
 * @throws Exception 异常
 */
void doPreProcess(TransferDTO requestDTO) throws Exception;
```
2.编写自定义Consumer后置处理器，继承com.xiaobai.nettyrpc.consumer.processor.ConsumerPostProcessor接口，实现doPostProcess方法，该方法描述如下：
```java
/**
 * 后置处理
 * @param responseDTO 返回DTO
 * @throws Exception 异常
 */
void doPostProcess(TransferDTO responseDTO) throws Exception;
```
3.在resources目录下新建META-INF/services/com.xiaobai.nettyrpc.consumer.processor.ConsumerPreProcessor文件，文件内容为自定义Consumer前置处理器全限定类名。  
4.在resources目录下新建META-INF/services/com.xiaobai.nettyrpc.consumer.processor.ConsumerPostProcessor文件，文件内容为自定义Consumer后置处理器全限定类名。  
5.在application.properties配置文件中指定Consumer前置处理链和后置处理链：
```properties
#Consumer前置处理链（前置处理器全限定类名），多个前置处理器逗号分隔，按先后顺序执行
netty-rpc.consumer-pre-processors=xxx,xxx
#Consumer后置处理链（后置处理器全限定类名），多个后置处理器逗号分隔，按先后顺序执行
netty-rpc.consumer-post-processors=xxx,xxx
```
## 1.2 Provider端
### 1.2.1 基础配置
application.properties配置文件中添加如下配置：
```properties
#Provider名称，为空则取spring.application.name
netty-rpc.name=provider-demo
#Provider netty server端口，为空则默认18317
netty-rpc.provider-port=1234
#nacos地址
netty-rpc.nacos-address=127.0.0.1:8848
#nacos namespace id，非必须，默认public namespace
netty-rpc.namespace-id=465d5b72-b6be-40bf-ba02-1cc1db659ef8
```
远程接口实现类上添加@Service注解（com.xiaobai.nettyrpc.provider.annotations.Service）:
```java
@Service
public class TestService implements Test {

    @Override
    public String test(String message) {
        return "hello " + message;
    }
}
```
### 1.2.2 指定实现类group和权重
@Service注解中可以指定实现类group和权重：
```java
@Service(group = "group1", weight = 10)
```
### 1.2.3 指定序列化方法
同1.1.6
### 1.2.4 自定义远程调用前置处理和后置处理
netty-rpc框架提供了Provider远程调用前置处理和后置处理机制，可以自定义前置处理器以及后置处理器，在实际调用接口方法之前和之后做处理，具体方法如下：  
1.编写自定义Provider前置处理器，继承com.xiaobai.nettyrpc.provider.processor.ProviderPreProcessor接口，实现doPreProcess方法，该方法描述如下：
```java
/**
 * 前置处理
 * @param requestDTO 请求DTO
 * @throws Exception 异常
 */
void doPreProcess(TransferDTO requestDTO) throws Exception;
```
2.编写自定义Provider后置处理器，继承com.xiaobai.nettyrpc.provider.processor.ProviderPostProcessor接口，实现doPostProcess方法，该方法描述如下：
```java
/**
 * 后置处理
 * @param responseDTO 返回DTO
 * @throws Exception 异常
 */
void doPostProcess(TransferDTO responseDTO) throws Exception;
```
3.在resources目录下新建META-INF/services/com.xiaobai.nettyrpc.provider.processor.ProviderPreProcessor文件，文件内容为自定义Provider前置处理器全限定类名。  
4.在resources目录下新建META-INF/services/com.xiaobai.nettyrpc.consumer.processor.ProviderPostProcessor文件，文件内容为自定义Provider后置处理器全限定类名。  
5.在application.properties配置文件中指定Provider前置处理链和后置处理链：
```properties
#Provider前置处理链（前置处理器全限定类名），多个前置处理器逗号分隔，按先后顺序执行
netty-rpc.provider-pre-processors=xxx,xxx
#Provider后置处理链（后置处理器全限定类名），多个后置处理器逗号分隔，按先后顺序执行
netty-rpc.provider-post-processors=xxx,xxx
```
### 1.2.5 指定Provider处理线程池参数
可以在application.properties配置文件中指定Provider处理线程池参数：
```properties
#核心线程数，默认200
netty-rpc.provider-core-pool-size=xx
#最大线程数，默认500
netty-rpc.provider-max-pool-size=xx
#队列长度，默认500
netty-rpc.provider-queue-capacity=xx
#空闲线程存活时长，默认10s
netty-rpc.provider-keep-alive-seconds=xx
```
## 1.3 Metric指标
netty-rpc框架提供prometheus exporter暴露metric指标能力，默认关闭，如需开启则可在application.properties添加以下配置：  
```properties
#exporter暴露端口，非必须，若不设置则为应用启动端口
management.server.port=1123
#开启prometheus exporter
management.endpoints.web.exposure.include=prometheus
```
暴露的具体metric以及描述如下：  
|metric|描述|类型|labels|
|-|-|-|-|
|remote_call_total|远程调用次数|Counter|provider_name(提供者名称),remote_address(远程服务地址),interface_name(远程调用接口名),group(接口实现类group),type(success或者fail)|
|remote_call_time_consume_range|远程调用耗时分布|Histogram|provider_name(提供者名称),remote_address(远程服务地址),interface_name(远程调用接口名),group(接口实现类group)|