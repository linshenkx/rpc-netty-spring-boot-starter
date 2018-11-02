package com.github.linshenkx.rpcnettyclientspringbootautoconfigure.handler;

import com.github.linshenkx.rpcnettycommon.bean.RpcResponse;
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
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private ConcurrentMap<String,RpcResponse> responseMap;

    public RpcClientHandler(ConcurrentMap<String,RpcResponse> responseMap){
        this.responseMap=responseMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        log.info("read a Response,requestId: "+msg.getRequestId());
        responseMap.put(msg.getRequestId(),msg);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        log.error("client caught exception",cause);
        ctx.close();
    }

}
