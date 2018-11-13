package com.github.linshenkx.rpcnettyclientspringbootautoconfigure.client;


import com.github.linshenkx.rpcnettyclientspringbootautoconfigure.ZKServiceDiscovery;
import com.github.linshenkx.rpcnettycommon.bean.RemotingTransporter;
import com.github.linshenkx.rpcnettycommon.bean.RpcRequest;
import com.github.linshenkx.rpcnettycommon.bean.RpcResponse;
import com.github.linshenkx.rpcnettycommon.codec.decode.RemotingTransporterDecoder;
import com.github.linshenkx.rpcnettycommon.codec.encode.RemotingTransporterEncoder;
import com.github.linshenkx.rpcnettycommon.handler.RpcClientHandler;
import com.github.linshenkx.rpcnettycommon.serialization.common.SerializeType;
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

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

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
    private ConcurrentMap<Long, RemotingTransporter> responseMap=new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass){
        //创建动态代理对象
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                (proxy, method, args) -> {
                    //创建RPC请求对象
                    RpcRequest rpcRequest=new RpcRequest();
                    rpcRequest.setInterfaceName(method.getDeclaringClass().getName());
                    rpcRequest.setMethodName(method.getName());
                    rpcRequest.setParameterTypes(method.getParameterTypes());
                    rpcRequest.setParameters(args);
                    //获取RPC服务地址
                    String serviceName=interfaceClass.getName();
                    List<String> addressList=zkServiceDiscovery.getAddressList(serviceName);
                    String serviceAddress=addressList.get(ThreadLocalRandom.current().nextInt(addressList.size()));

                    RemotingTransporter remotingTransporter=RemotingTransporter.builder().build();
                    remotingTransporter.setFlag((byte) 1);
                    remotingTransporter.setBodyContent(rpcRequest);

                    log.info("get serviceAddres:"+serviceAddress);
                    //从RPC服务地址中解析主机名与端口号
                    String[] stringArray= StringUtils.split(serviceAddress,":");
                    String host= Objects.requireNonNull(stringArray)[0];
                    int port=Integer.parseInt(stringArray[1]);
                    //发送RPC请求
                    RemotingTransporter rpcResponse=send(remotingTransporter,host,port);
                    //获取响应结果
                    if(rpcResponse==null){
                        log.error("send request failure",new IllegalStateException("response is null"));
                        return null;
                    }
                    RpcResponse rpcResponse1= (RpcResponse) rpcResponse.getBodyContent();
                    if(rpcResponse1.getException()!=null){
                        log.error("response has exception",rpcResponse1.getException());
                        return null;
                    }
                    return rpcResponse1.getResult();
                }
        );
    }


    private RemotingTransporter send(RemotingTransporter remotingTransporter,String host,int port){
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
                    pipeline.addLast(new RemotingTransporterDecoder(SerializeType.ProtoStuffSerializer,RpcResponse.class))
                            .addFirst(new RemotingTransporterEncoder(SerializeType.ProtoStuffSerializer))
                            .addLast(new RpcClientHandler(responseMap));
                }
            });
            ChannelFuture future=bootstrap.connect(host,port).sync();
            log.info("invokeId: "+remotingTransporter.getInvokeId());
            //写入RPC请求对象
            Channel channel=future.channel();
            channel.writeAndFlush(remotingTransporter).sync();
            channel.closeFuture().sync();
            log.info("send end");
            //获取RPC响应对象
            return responseMap.get(remotingTransporter.getInvokeId());
        }catch (Exception e){
            log.error("client exception",e);
            return null;
        }finally {
            group.shutdownGracefully();
            //移除请求编号和响应对象直接的映射关系
            responseMap.remove(remotingTransporter.getInvokeId());
        }

    }

}
