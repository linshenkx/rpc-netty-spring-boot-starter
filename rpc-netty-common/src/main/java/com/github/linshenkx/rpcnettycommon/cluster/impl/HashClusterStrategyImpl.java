package com.github.linshenkx.rpcnettycommon.cluster.impl;


import com.github.linshenkx.rpcnettycommon.bean.ServiceInfo;
import com.github.linshenkx.rpcnettycommon.cluster.ClusterStrategy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * 软负载哈希算法实现
 *
 * @author liyebing created on 17/4/23.
 * @version $Id$
 */
public class HashClusterStrategyImpl implements ClusterStrategy {

    @Override
    public ServiceInfo select(List<ServiceInfo> providerServices) {
        //获取调用方ip
        String localIP = "127.0.0.1";
        try {
            localIP = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        //获取源地址对应的hashcode
        int hashCode = localIP.hashCode();
        //获取服务列表大小
        int size = providerServices.size();

        return providerServices.get(hashCode % size);
    }
}
