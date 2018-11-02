package com.linshen.rpcprovider.Impl;


import com.linshen.rpclib.HelloService;
import com.linshen.rpcnettyserverspringbootautoconfigure.annotation.RpcService;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {

  @Override
  public String say(String name) {
    return "hello " + name;
  }

}
