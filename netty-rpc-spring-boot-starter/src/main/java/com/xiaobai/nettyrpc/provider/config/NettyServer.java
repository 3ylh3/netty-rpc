package com.xiaobai.nettyrpc.provider.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.xiaobai.nettyrpc.codec.AbstractDecoder;
import com.xiaobai.nettyrpc.codec.AbstractEncoder;
import com.xiaobai.nettyrpc.codec.HessianDecoder;
import com.xiaobai.nettyrpc.codec.HessianEncoder;
import com.xiaobai.nettyrpc.common.constants.CommonConstants;
import com.xiaobai.nettyrpc.common.properties.NettyRpcProperties;
import com.xiaobai.nettyrpc.common.utils.NetWorkUtil;
import com.xiaobai.nettyrpc.common.utils.SPIUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * netty server
 *
 * @author yinzhaojing
 * @date 2022-06-24 15:22:35
 */
public class NettyServer implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    @Autowired
    private NettyRpcProperties nettyRpcProperties;
    @Autowired
    private NettyServerHandler nettyServerHandler;
    @Autowired
    private NamingService namingService;
    @Value("${spring.application.name}")
    private String applicationName;


    /**
     * 初始化netty server
     * @param args 参数
     */
    @Override
    public void run(ApplicationArguments args) {
        // 如果有远程服务需要暴露，则启动netty server
        if (ProviderServiceCache.isEmpty()) {
            return;
        }
        int port = null == nettyRpcProperties.getProviderPort() ? CommonConstants.DEFAULT_PORT
                : nettyRpcProperties.getProviderPort();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            logger.info("start init server,port:{}", port);
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE,
                                    Delimiters.lineDelimiter()[0]));
                            AbstractDecoder decoder = SPIUtil.getObject(nettyRpcProperties.getDecodeClassName(),
                                    AbstractDecoder.class);
                            if (null == decoder) {
                                decoder = new HessianDecoder();
                            }
                            socketChannel.pipeline().addLast(decoder);
                            socketChannel.pipeline().addLast(nettyServerHandler);
                            AbstractEncoder encoder = SPIUtil.getObject(nettyRpcProperties.getEncodeClassName(),
                                    AbstractEncoder.class);
                            if (null == encoder) {
                                encoder = new HessianEncoder();
                            }
                            socketChannel.pipeline().addLast(encoder);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            // 注册服务到注册中心
            registerServices(port);
            // 启动server
            serverBootstrap.bind(port).sync().channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("init server exception:", e);
            System.exit(-1);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    /**
     * 注册服务到注册中心
     * @param port 端口
     * @throws Exception
     */
    private void registerServices(Integer port) throws Exception {
        JSONArray services = ProviderServiceCache.getServices();
        if (!services.isEmpty()) {
            Instance instance = new Instance();
            instance.setIp(NetWorkUtil.getLocalIp());
            instance.setPort(port);
            instance.setHealthy(true);
            instance.addMetadata(CommonConstants.SERVICES, services.toString());
            String providerName = StringUtils.isBlank(nettyRpcProperties.getName()) ? applicationName
                    : nettyRpcProperties.getName();
            namingService.registerInstance(providerName, instance);
            logger.info("register services success");
        }
    }
}
