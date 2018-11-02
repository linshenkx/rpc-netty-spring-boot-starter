package com.github.linshenkx.rpcnettyserverspringbootautoconfigure.registry;


import com.github.linshenkx.rpcnettyserverspringbootautoconfigure.properties.ZKProperties;
import lombok.extern.log4j.Log4j2;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 2018/10/31
 * @Description: zookeeper服务注册中心
 */
@Log4j2
@EnableConfigurationProperties(ZKProperties.class)
public class ZKServiceRegistry {


  @Autowired
  private ZKProperties zkProperties;

  private ZkClient zkClient;

  @PostConstruct
  public void init() {
    // 创建 ZooKeeper 客户端
    zkClient = new ZkClient(getAddress(zkProperties.getAddressList()), zkProperties.getSessionTimeOut(), zkProperties.getConnectTimeOut());
    log.info("connect to zookeeper");
  }

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
   * 为服务端提供注册
   * 将服务地址注册到对应服务名下
   * 断开连接后地址自动清除
   * @param serviceName
   * @param serviceAddress
   */
  public void register(String serviceName, String serviceAddress) {
    // 创建 registry 节点（持久）
    String registryPath = zkProperties.getRegistryPath();
    if (!zkClient.exists(registryPath)) {
      zkClient.createPersistent(registryPath);
      log.info("create registry node: {}", registryPath);
    }
    // 创建 service 节点（持久）
    String servicePath = registryPath + "/" + serviceName;
    if (!zkClient.exists(servicePath)) {
      zkClient.createPersistent(servicePath);
      log.info("create service node: {}", servicePath);
    }
    // 创建 address 节点（临时）
    String addressPath = servicePath + "/address-";
    String addressNode = zkClient.createEphemeralSequential(addressPath, serviceAddress);
    log.info("create address node: {}", addressNode);
  }



}