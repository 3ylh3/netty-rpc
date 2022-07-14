package com.xiaobai.nettyrpc.provider.config;

import com.xiaobai.nettyrpc.common.properties.NettyRpcProperties;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * netty server handler
 *
 * @author yinzhaojing
 * @date 2022-06-20 19:49:06
 */
@ChannelHandler.Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    @Autowired
    private NettyRpcProperties nettyRpcProperties;
    @Autowired
    private AsyncProcessor asyncProcessor;



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 线程池异步处理
        asyncProcessor.process(ctx, msg, nettyRpcProperties.getProviderPreProcessors(),
                nettyRpcProperties.getProviderPreProcessorsParams(), nettyRpcProperties.getProviderPostProcessors(),
                nettyRpcProperties.getProviderPostProcessorsParams());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("exception:", cause);
        ctx.close();
    }
}
