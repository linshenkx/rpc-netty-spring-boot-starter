package com.github.linshenkx.rpcprovider.Impl;


import com.github.linshenkx.rpclib.HelloService;
import com.github.linshenkx.rpcnettycommon.annotation.RpcService;

@RpcService(value = HelloService.class,weight = 2,workerThreads = 1)
public class HelloServiceImpl implements HelloService {

  @Override
  public String say(String name) {
    return "hello " + name;
  }

}
