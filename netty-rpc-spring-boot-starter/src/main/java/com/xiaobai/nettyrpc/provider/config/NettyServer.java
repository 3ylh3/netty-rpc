package com.xiaobai.nettyrpc.provider.config;

import com.xiaobai.nettyrpc.codec.AbstractDecoder;
import com.xiaobai.nettyrpc.codec.AbstractEncoder;
import com.xiaobai.nettyrpc.codec.HessianDecoder;
import com.xiaobai.nettyrpc.codec.HessianEncoder;
import com.xiaobai.nettyrpc.common.constants.CommonConstants;
import com.xiaobai.nettyrpc.common.properties.NettyRpcProperties;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
        logger.info("start init server,port:{}", port);
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
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
                            socketChannel.pipeline().addLast(new NettyServerHandler());
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
            serverBootstrap.bind(port).sync().channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("init server exception:", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
