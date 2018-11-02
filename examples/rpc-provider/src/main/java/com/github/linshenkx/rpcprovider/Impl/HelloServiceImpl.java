package com.github.linshenkx.rpcprovider.Impl;


import com.github.linshenkx.rpclib.HelloService;
import com.github.linshenkx.rpcnettyserverspringbootautoconfigure.annotation.RpcService;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {

  @Override
  public String say(String name) {
    return "hello " + name;
  }

}
