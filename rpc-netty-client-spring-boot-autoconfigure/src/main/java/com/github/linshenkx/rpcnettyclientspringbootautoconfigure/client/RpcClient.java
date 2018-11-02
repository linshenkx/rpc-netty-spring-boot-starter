package com.github.linshenkx.rpcnettyclientspringbootautoconfigure.client;


import com.github.linshenkx.rpcnettyclientspringbootautoconfigure.discovery.ZKServiceDiscovery;
import com.github.linshenkx.rpcnettyclientspringbootautoconfigure.handler.RpcClientHandler;
import com.github.linshenkx.rpcnettycommon.bean.RpcRequest;
import com.github.linshenkx.rpcnettycommon.bean.RpcResponse;
import com.github.linshenkx.rpcnettycommon.codec.RpcDecoder;
import com.github.linshenkx.rpcnettycommon.codec.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 2018/11/1
 * @Description: TODO
 */
@Log4j2
@Component
@AutoConfigureAfter(ZKServiceDiscovery.class)
public class RpcClient {
    @Autowired
    private ZKServiceDiscovery zkServiceDiscovery;
    /**
     * 存放请求编号与响应对象的映射关系
     */
    private ConcurrentMap<String, RpcResponse> responseMap=new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass){
        //创建动态代理对象
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        //创建RPC请求对象
                        RpcRequest rpcRequest=new RpcRequest();
                        rpcRequest.setRequestId(UUID.randomUUID().toString());
                        rpcRequest.setInterfaceName(method.getDeclaringClass().getName());
                        rpcRequest.setMethodName(method.getName());
                        rpcRequest.setParameterTypes(method.getParameterTypes());
                        rpcRequest.setParameters(args);
                        //获取RPC服务地址
                        String serviceName=interfaceClass.getName();
                        String serviceAddress=zkServiceDiscovery.discover(serviceName);
                        log.info("get serviceAddres:"+serviceAddress);
                        //从RPC服务地址中解析主机名与端口号
                        String[] stringArray= StringUtils.split(serviceAddress,":");
                        String host= Objects.requireNonNull(stringArray)[0];
                        int port=Integer.parseInt(stringArray[1]);
                        //发送RPC请求
                        RpcResponse rpcResponse=send(rpcRequest,host,port);
                        //获取响应结果
                        if(rpcResponse==null){
                            log.error("send request failure",new IllegalStateException("response is null"));
                            return null;
                        }
                        if(rpcResponse.getException()!=null){
                            log.error("response has exception",rpcResponse.getException());
                            return null;
                        }
                        return rpcResponse.getResult();
                    }
                }
        );
    }


    private RpcResponse send(RpcRequest rpcRequest,String host,int port){
        log.info("send begin: "+host+":"+port);
        //客户端线程为1即可
        EventLoopGroup group=new NioEventLoopGroup(1);
        try {
            //创建RPC连接
            Bootstrap bootstrap=new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel) throws Exception {
                    ChannelPipeline pipeline=channel.pipeline();
                    pipeline.addLast(new RpcEncoder(RpcRequest.class))
                            .addLast(new RpcDecoder(RpcResponse.class))
                            .addLast(new RpcClientHandler(responseMap));
                }
            });
            ChannelFuture future=bootstrap.connect(host,port).sync();
            log.info("requestId: "+rpcRequest.getRequestId());
            //写入RPC请求对象
            Channel channel=future.channel();
            channel.writeAndFlush(rpcRequest).sync();
            channel.closeFuture().sync();
            log.info("send end");
            //获取RPC响应对象
            return responseMap.get(rpcRequest.getRequestId());
        }catch (Exception e){
            log.error("client exception",e);
            return null;
        }finally {
            group.shutdownGracefully();
            //移除请求编号和响应对象直接的映射关系
            responseMap.remove(rpcRequest.getRequestId());
        }

    }

}
