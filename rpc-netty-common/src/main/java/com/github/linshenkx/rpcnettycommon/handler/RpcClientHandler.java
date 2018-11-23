package com.github.linshenkx.rpcnettycommon.handler;

import com.github.linshenkx.rpcnettycommon.protocal.xuan.RemotingTransporter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ConcurrentMap;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 2018/11/1
 * @Description: TODO
 */
@Log4j2
public class RpcClientHandler extends SimpleChannelInboundHandler<RemotingTransporter> {

    private ConcurrentMap<Long,RemotingTransporter> remotingTransporterMap;

    public RpcClientHandler(ConcurrentMap<Long,RemotingTransporter> remotingTransporterMap){
        this.remotingTransporterMap=remotingTransporterMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RemotingTransporter remotingTransporter) throws Exception {
        log.info("read a Response,invokeId: "+remotingTransporter.getInvokeId());
        remotingTransporterMap.put(remotingTransporter.getInvokeId(),remotingTransporter);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        log.error("client caught exception",cause);
        ctx.close();
    }


}
