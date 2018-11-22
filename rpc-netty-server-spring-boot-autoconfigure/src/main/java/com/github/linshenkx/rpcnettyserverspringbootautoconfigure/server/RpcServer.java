package com.github.linshenkx.rpcnettyserverspringbootautoconfigure.server;

import com.alibaba.fastjson.JSON;
import com.github.linshenkx.rpcnettycommon.annotation.RpcService;
import com.github.linshenkx.rpcnettycommon.bean.ServiceInfo;
import com.github.linshenkx.rpcnettycommon.codec.decode.RemotingTransporterDecoder;
import com.github.linshenkx.rpcnettycommon.codec.encode.RemotingTransporterEncoder;
import com.github.linshenkx.rpcnettycommon.handler.RpcServerHandler;
import com.github.linshenkx.rpcnettyserverspringbootautoconfigure.properties.RpcServerProperties;
import com.github.linshenkx.rpcnettyserverspringbootautoconfigure.registry.zookeeper.ZKServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 2018/10/31
 * @Description: TODO
 */
@Log4j2
@AutoConfigureAfter({ZKServiceRegistry.class})
@EnableConfigurationProperties(RpcServerProperties.class)
public class RpcServer implements ApplicationContextAware, InitializingBean {

    /**
     * 存放 服务名称 与 服务实例 之间的映射关系
     */
    private Map<String,Object> handlerMap=new HashMap<>();

    /**
     * 存放 服务名称 与 信号量 之间的映射关系
     * 用于限制每个服务的工作线程数
     */
    private Map<String, Semaphore> serviceSemaphoreMap=new HashMap<>();

    /**
     * 存放 服务名称 与 服务信息 之间的映射关系
     * 用于限制每个服务的工作线程数
     */
    private Map<String, RpcService> serviceRpcServiceMap=new HashMap<>();

    @Autowired
    private RpcServerProperties rpcProperties;

    @Autowired
    private ZKServiceRegistry rpcServiceRegistry;

    /**
     * 在类初始化时执行，将所有被@RpcService标记的类纳入管理
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        //获取带有@RpcService注解的类
        Map<String,Object> rpcServiceMap=applicationContext.getBeansWithAnnotation(RpcService.class);
        //以@RpcService注解的value的类的类名为键将该标记类存入handlerMap和serviceSemaphoreMap
        if(!CollectionUtils.isEmpty(rpcServiceMap)){
            for(Object object:rpcServiceMap.values()){
                RpcService rpcService=object.getClass().getAnnotation(RpcService.class);
                String serviceName=rpcService.value().getName();
                handlerMap.put(serviceName,object);
                serviceSemaphoreMap.put(serviceName,new Semaphore(rpcService.workerThreads()));
                serviceRpcServiceMap.put(serviceName,rpcService);
            }
        }

    }


    /**
     * 在所有属性值设置完成后执行，负责启动RPC服务
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        //管理相关childGroup
        EventLoopGroup bossGroup=new NioEventLoopGroup();
        //处理相关RPC请求
        EventLoopGroup childGroup=new NioEventLoopGroup();

        try {
            //启动RPC服务
            ServerBootstrap bootstrap=new ServerBootstrap();
            bootstrap.group(bossGroup,childGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.option(ChannelOption.SO_BACKLOG,1024)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .handler(new LoggingHandler(LogLevel.INFO));
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel) throws Exception {
                    ChannelPipeline pipeline=channel.pipeline();
                    //解码RPC请求
                    pipeline.addLast(new RemotingTransporterDecoder());
                    //编码RPC请求
                    pipeline.addFirst(new RemotingTransporterEncoder());
                    //处理RPC请求
                    pipeline.addLast(new RpcServerHandler(handlerMap,serviceSemaphoreMap,serviceRpcServiceMap));
                }
            });
            //同步启动，RPC服务器启动完毕后才执行后续代码
            ChannelFuture future=bootstrap.bind(rpcProperties.getPort()).sync();
            log.info("server started,listening on {}",rpcProperties.getPort());

            //启动后注册服务
            registry();

            //释放资源
            future.channel().closeFuture().sync();
        }catch (Exception e){
            log.entry("server exception",e);
        }finally {
            //关闭RPC服务
            childGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }

    private void registry() throws UnknownHostException {
        //注册RPC服务地址
        String hostAddress=InetAddress.getLocalHost().getHostAddress();
        int port=rpcProperties.getPort();

        for(String interfaceName:handlerMap.keySet()){
            ServiceInfo serviceInfo=
                    new ServiceInfo(hostAddress,port,serviceRpcServiceMap.get(interfaceName).weight(),serviceRpcServiceMap.get(interfaceName).workerThreads());
            String serviceInfoString= JSON.toJSONString(serviceInfo);
            rpcServiceRegistry.register(interfaceName,serviceInfoString);
            log.info("register service:{}=>{}",interfaceName,serviceInfoString);
        }
    }
}
