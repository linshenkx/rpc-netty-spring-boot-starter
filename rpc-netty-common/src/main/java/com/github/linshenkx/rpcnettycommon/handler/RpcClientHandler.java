package com.github.linshenkx.rpcnettycommon.handler;

import com.github.linshenkx.rpcnettycommon.bean.RemotingTransporter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 2018/11/1
 * @Description: TODO
 */
public class RpcClientHandler extends SimpleChannelInboundHandler<RemotingTransporter> {
    private static final Logger logger = LoggerFactory.getLogger(RpcClientHandler.class);

    private ConcurrentMap<Long,RemotingTransporter> responseMap;

    public RpcClientHandler(ConcurrentMap<Long,RemotingTransporter> responseMap){
        this.responseMap=responseMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RemotingTransporter remotingTransporter) throws Exception {
        logger.info("read a Response,invokeId: "+remotingTransporter.getInvokeId());
        responseMap.put(remotingTransporter.getInvokeId(),remotingTransporter);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        logger.error("client caught exception",cause);
        ctx.close();
    }


}
