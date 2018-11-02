package com.github.linshenkx.rpcnettyclientspringbootautoconfigure.discovery;


import com.github.linshenkx.rpcnettyclientspringbootautoconfigure.properties.ZKProperties;
import lombok.extern.log4j.Log4j2;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 2018/10/31
 * @Description: zookeeper服务注册中心
 */
@Component
@Log4j2
@EnableConfigurationProperties(ZKProperties.class)
public class ZKServiceDiscovery {

  @Autowired
  private ZKProperties zkProperties;

  public String getAddress(List<String> addressList){
    if(CollectionUtils.isEmpty(addressList)){
      String defaultAddress="localhost:2181";
      log.error("addressList is empty,using defaultAddress:"+defaultAddress);
      return defaultAddress;
    }
    //待改进策略
    String address= getRandomAddress(addressList);
    log.info("using address:"+address);
    return address;
  }

  private String getRandomAddress(List<String> addressList){
    return addressList.get(ThreadLocalRandom.current().nextInt(addressList.size()));
  }


  /**
   * 为客户端提供地址
   * 根据服务名获取服务地址
   * @param serviceName
   * @return
   */
  public String discover(String serviceName){
    ZkClient zkClient = new ZkClient(getAddress(zkProperties.getAddressList()), zkProperties.getSessionTimeOut(), zkProperties.getConnectTimeOut());
    try {
      String servicePath=zkProperties.getRegistryPath()+"/"+serviceName;
      //找不到对应服务
      if(!zkClient.exists(servicePath)){
        throw new RuntimeException("can not find any service node on path: "+servicePath);
      }
      //该服务下无节点可用
      List<String> addressList=zkClient.getChildren(servicePath);
      if(CollectionUtils.isEmpty(addressList)){
        throw new RuntimeException("can not find any address node on path: "+servicePath);
      }
      //获取address节点地址
      String address=getRandomAddress(addressList);
      //获取address节点的值
      return zkClient.readData(servicePath+"/"+address);
    }finally {
      zkClient.close();
    }
  }

}