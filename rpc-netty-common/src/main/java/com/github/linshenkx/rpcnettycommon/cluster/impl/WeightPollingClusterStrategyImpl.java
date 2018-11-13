package com.github.linshenkx.rpcnettycommon.cluster.impl;


import avro.shaded.com.google.common.collect.Lists;
import com.github.linshenkx.rpcnettycommon.bean.ServiceInfo;
import com.github.linshenkx.rpcnettycommon.cluster.ClusterStrategy;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 软负载实现加权轮询算法实现
 *
 * @author liyebing created on 17/4/23.
 * @version $Id$
 */
public class WeightPollingClusterStrategyImpl implements ClusterStrategy {

    //计数器
    private int index = 0;
    //计数器锁
    private Lock lock = new ReentrantLock();

    @Override
    public ServiceInfo select(List<ServiceInfo> providerServices) {

        ServiceInfo service = null;
        try {
            lock.tryLock(10, TimeUnit.MILLISECONDS);
            //存放加权后的服务提供者列表
            List<ServiceInfo> providerList = Lists.newArrayList();
            for (ServiceInfo provider : providerServices) {
                int weight = provider.getWeight();
                for (int i = 0; i < weight; i++) {
                    providerList.add((ServiceInfo) provider.clone());
                }
            }
            //若计数大于服务提供者个数,将计数器归0
            if (index >= providerList.size()) {
                index = 0;
            }
            service = providerList.get(index);
            index++;
            return service;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        //兜底,保证程序健壮性,若未取到服务,则直接取第一个
        return providerServices.get(0);
    }
}
