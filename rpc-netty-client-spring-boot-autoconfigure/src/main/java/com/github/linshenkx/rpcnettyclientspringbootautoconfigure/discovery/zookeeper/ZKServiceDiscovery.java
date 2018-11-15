package com.github.linshenkx.rpcnettyclientspringbootautoconfigure.discovery.zookeeper;


import com.github.linshenkx.rpcnettyclientspringbootautoconfigure.properties.ZKProperties;
import lombok.extern.log4j.Log4j2;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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



  /**
   * 服务名和服务地址列表的Map
   */
  private ConcurrentMap<String,List<String>> servicePathsMap=new ConcurrentHashMap<>();

  /**
   * 服务监听器 Map,监听子节点服务信息
   */
  private ConcurrentMap<String, IZkChildListener> zkChildListenerMap=new ConcurrentHashMap<>();

  private ZkClient zkClient;

  @PostConstruct
  public void init() {
    // 创建 ZooKeeper 客户端
    zkClient = new ZkClient(zkProperties.getAddress(), zkProperties.getSessionTimeOut(), zkProperties.getConnectTimeOut());
    log.info("connect to zookeeper");
  }

  /**
   *
   * 根据服务名获取服务地址并保持监控
   * @param serviceName
   * @return
   */
  public void discover(String serviceName){
    log.info("discovering:"+serviceName);
    String servicePath=zkProperties.getRegistryPath()+"/"+serviceName;
    //找不到对应服务
    if(!zkClient.exists(servicePath)){
      throw new RuntimeException("can not find any service node on path: "+servicePath);
    }
    //获取服务地址列表
    List<String> addressList=zkClient.getChildren(servicePath);
    if(CollectionUtils.isEmpty(addressList)){
      throw new RuntimeException("can not find any address node on path: "+servicePath);
    }
    //保存地址列表
    List<String> paths=new ArrayList<>(addressList.size());
    for(String address:addressList){
      paths.add(zkClient.readData(servicePath+"/"+address));
    }
    servicePathsMap.put(serviceName,paths);
    //保持监控
    if(!zkChildListenerMap.containsKey(serviceName)){
      IZkChildListener iZkChildListener= (parentPath, currentChilds) -> {
        //当子节点列表变化时重新discover
        discover(serviceName);
        log.info("子节点列表发生变化 ");
      };
      zkClient.subscribeChildChanges(servicePath, iZkChildListener);
      zkChildListenerMap.put(serviceName,iZkChildListener);
    }
  }

  public List<String> getAddressList(String serviceName){
      List<String> addressList=servicePathsMap.get(serviceName);
      if(addressList==null||addressList.isEmpty()){
          discover(serviceName);
          return servicePathsMap.get(serviceName);
      }
      return addressList;
  }

}