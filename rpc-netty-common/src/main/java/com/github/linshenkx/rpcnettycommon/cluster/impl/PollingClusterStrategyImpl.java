package com.github.linshenkx.rpcnettycommon.cluster.impl;


import com.github.linshenkx.rpcnettycommon.bean.ServiceInfo;
import com.github.linshenkx.rpcnettycommon.cluster.ClusterStrategy;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 软负载轮询算法实现
 *
 * @author liyebing created on 17/4/23.
 * @version $Id$
 */
public class PollingClusterStrategyImpl implements ClusterStrategy {

    //计数器
    private int index = 0;
    private Lock lock = new ReentrantLock();

    @Override
    public ServiceInfo select(List<ServiceInfo> providerServices) {

        ServiceInfo service = null;
        try {
            lock.tryLock(10, TimeUnit.MILLISECONDS);
            //若计数大于服务提供者个数,将计数器归0
            if (index >= providerServices.size()) {
                index = 0;
            }
            service = providerServices.get(index);
            index++;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        //兜底,保证程序健壮性,若未取到服务,则直接取第一个
        if (service == null) {
            service = providerServices.get(0);
        }
        return service;
    }
}
