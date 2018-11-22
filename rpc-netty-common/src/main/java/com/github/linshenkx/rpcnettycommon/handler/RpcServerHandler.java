package com.github.linshenkx.rpcnettycommon.handler;


import com.github.linshenkx.rpcnettycommon.annotation.RpcService;
import com.github.linshenkx.rpcnettycommon.bean.RpcRequest;
import com.github.linshenkx.rpcnettycommon.bean.RpcResponse;
import com.github.linshenkx.rpcnettycommon.protocal.xuan.RemotingTransporter;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 2018/10/31
 * @Description: RPC服务端处理器（处理RpcRequest）
 */
@Log4j2
public class RpcServerHandler extends SimpleChannelInboundHandler<RemotingTransporter> {

  /**
   * 存放 服务名称 与 服务实例 之间的映射关系
   */
  private final Map<String, Object> handlerMap;

  /**
   * 存放 服务名称 与 信号量 之间的映射关系
   * 用于限制每个服务的工作线程数
   */
  private final Map<String, Semaphore> serviceSemaphoreMap;

  /**
   * 存放 服务名称 与 服务信息 之间的映射关系
   * 用于限制每个服务的工作线程数
   */
  private final Map<String, RpcService> serviceRpcServiceMap;

  public RpcServerHandler(Map<String, Object> handlerMap,Map<String, Semaphore> serviceSemaphoreMap,Map<String, RpcService> serviceRpcServiceMap) {
    this.handlerMap = handlerMap;
    this.serviceSemaphoreMap=serviceSemaphoreMap;
    this.serviceRpcServiceMap=serviceRpcServiceMap;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, RemotingTransporter remotingTransporter) throws Exception {
    log.info("channelRead0 begin");
    remotingTransporter.setFlag(new RemotingTransporter.Flag(false,true,false,false,remotingTransporter.getFlag().getSerializeType()));
    RpcResponse rpcResponse=new RpcResponse();
    RpcRequest rpcRequest=(RpcRequest)remotingTransporter.getBodyContent();
    Semaphore semaphore = serviceSemaphoreMap.get(rpcRequest.getInterfaceName());
    boolean acquire=false;
        try {
        // 处理 RPC 请求成功
        log.info("进入限流");
        acquire=semaphore.tryAcquire();
        if(acquire){
          Object result= handle(rpcRequest);
          rpcResponse.setResult(result);
        }

      } catch (Exception e) {
        // 处理 RPC 请求失败
        rpcResponse.setException(e);
        log.error("handle result failure", e);
      } finally {
        if(acquire){
          semaphore.release();
          log.info("释放信号量");
        }
      }
    remotingTransporter.setBodyContent(rpcResponse);
    channelHandlerContext.writeAndFlush(remotingTransporter).addListener(ChannelFutureListener.CLOSE);
  }


  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    log.error("server caught exception", cause);
    ctx.close();
  }

  private Object handle(RpcRequest request) throws Exception {
    log.info("开始执行handle");
    // 获取服务实例
    String serviceName = request.getInterfaceName();
    Object serviceBean = handlerMap.get(serviceName);
    if (serviceBean == null) {
      throw new RuntimeException(String.format("can not find service bean by key: %s", serviceName));
    }
    // 获取反射调用所需的变量
    Class<?> serviceClass = serviceBean.getClass();
    String methodName = request.getMethodName();
    log.info(methodName);
    Class<?>[] parameterTypes = request.getParameterTypes();
    log.info(parameterTypes[0].getName());
    Object[] parameters = request.getParameters();
    // 执行反射调用
    Method method = serviceClass.getMethod(methodName, parameterTypes);
    method.setAccessible(true);
    log.info(parameters[0].toString());
    return method.invoke(serviceBean, parameters);
  }


}
