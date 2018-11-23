package com.github.linshenkx.rpcnettyclientspringbootautoconfigure.client;


import com.alibaba.fastjson.JSON;
import com.github.linshenkx.rpcnettyclientspringbootautoconfigure.discovery.zookeeper.ZKServiceDiscovery;
import com.github.linshenkx.rpcnettyclientspringbootautoconfigure.properties.RpcClientProperties;
import com.github.linshenkx.rpcnettycommon.bean.RpcRequest;
import com.github.linshenkx.rpcnettycommon.bean.RpcResponse;
import com.github.linshenkx.rpcnettycommon.bean.ServiceInfo;
import com.github.linshenkx.rpcnettycommon.codec.decode.RemotingTransporterDecoder;
import com.github.linshenkx.rpcnettycommon.codec.encode.RemotingTransporterEncoder;
import com.github.linshenkx.rpcnettycommon.handler.RpcClientHandler;
import com.github.linshenkx.rpcnettycommon.protocal.xuan.RemotingTransporter;
import com.github.linshenkx.rpcnettycommon.route.RouteEngine;
import com.github.linshenkx.rpcnettycommon.route.RouteStrategy;
import com.github.linshenkx.rpcnettycommon.route.RouteStrategyEnum;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
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
@EnableConfigurationProperties(RpcClientProperties.class)
public class RpcClient {

    @Autowired
    private ZKServiceDiscovery zkServiceDiscovery;

    @Autowired
    private RpcClientProperties rpcClientProperties;

    /**
     * 维持服务的 轮询 路由状态
     * 不同服务状态不同（服务列表也不同）
     * 非轮询无需维持状态
     */
    private ConcurrentMap<String,RouteStrategy> serviceRouteStrategyMap=new ConcurrentHashMap<>();

    /**
     * 存放请求编号与响应对象的映射关系
     */
    private ConcurrentMap<Long, RemotingTransporter> remotingTransporterMap=new ConcurrentHashMap<>();


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
                    //获取RPC服务信息列表
                    String serviceName=interfaceClass.getName();
                    List<String> addressList=zkServiceDiscovery.getAddressList(serviceName);

                    List<ServiceInfo> serviceInfoList=new ArrayList<>(addressList.size());
                    for(String serviceInfoString:addressList){
                        serviceInfoList.add(JSON.parseObject(serviceInfoString,ServiceInfo.class));
                    }
                    //根据配置文件获取路由策略
                    log.info("使用负载均衡策略："+rpcClientProperties.getRouteStrategy());
                    log.info("使用序列化策略："+rpcClientProperties.getSerializeType());
                    RouteStrategy routeStrategy ;
                    //如果使用轮询，则需要保存状态（按服务名保存）
                    if(RouteStrategyEnum.Polling==rpcClientProperties.getRouteStrategy()){
                        routeStrategy=serviceRouteStrategyMap.getOrDefault(serviceName,RouteEngine.queryClusterStrategy(RouteStrategyEnum.Polling));
                        serviceRouteStrategyMap.put(serviceName,routeStrategy);
                    }else {
                        routeStrategy= RouteEngine.queryClusterStrategy(rpcClientProperties.getRouteStrategy());
                    }
                    //根据路由策略选取服务提供方
                    ServiceInfo serviceInfo = routeStrategy.select(serviceInfoList);

                    RemotingTransporter remotingTransporter=new RemotingTransporter();
                    //设置flag为请求，双路，非ping，非其他，序列化方式为 配置文件中SerializeTypeEnum对应的code
                    remotingTransporter.setFlag(new RemotingTransporter.Flag(true,true,false,false,rpcClientProperties.getSerializeType().getCode()));

                    remotingTransporter.setBodyContent(rpcRequest);

                    log.info("get serviceInfo:"+serviceInfo);
                    //从RPC服务地址中解析主机名与端口号
                    //发送RPC请求
                    RpcResponse rpcResponse=send(remotingTransporter,serviceInfo.getHost(),serviceInfo.getPort());
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
        );
    }


    private RpcResponse send(RemotingTransporter remotingTransporter,String host,int port){
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
                    pipeline.addLast(new RemotingTransporterDecoder())
                            .addFirst(new RemotingTransporterEncoder())
                            .addLast(new RpcClientHandler(remotingTransporterMap));
                }
            });
            ChannelFuture future=bootstrap.connect(host,port).sync();
            Channel channel=future.channel();
            log.info("invokeId: "+remotingTransporter.getInvokeId());
            //写入RPC请求对象
            channel.writeAndFlush(remotingTransporter).sync();
            channel.closeFuture().sync();
            log.info("send end");
            //获取RPC响应对象
            return (RpcResponse) remotingTransporterMap.get(remotingTransporter.getInvokeId()).getBodyContent();
        }catch (Exception e){
            log.error("client exception",e);
            return null;
        }finally {
            group.shutdownGracefully();
            //移除请求编号和响应对象直接的映射关系
            remotingTransporterMap.remove(remotingTransporter.getInvokeId());
        }

    }


}
