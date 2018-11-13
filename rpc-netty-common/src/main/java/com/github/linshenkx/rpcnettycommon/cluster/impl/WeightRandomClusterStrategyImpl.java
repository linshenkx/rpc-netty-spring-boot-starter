package com.github.linshenkx.rpcnettycommon.cluster.impl;


import avro.shaded.com.google.common.collect.Lists;
import com.github.linshenkx.rpcnettycommon.bean.ServiceInfo;
import com.github.linshenkx.rpcnettycommon.cluster.ClusterStrategy;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;

/**
 * 软负载加权随机算法实现
 *
 * @author liyebing created on 17/4/23.
 * @version $Id$
 */
public class WeightRandomClusterStrategyImpl implements ClusterStrategy {


    @Override
    public ServiceInfo select(List<ServiceInfo> providerServices) {
        //存放加权后的服务提供者列表
        List<ServiceInfo> providerList = Lists.newArrayList();
        for (ServiceInfo provider : providerServices) {
            int weight = provider.getWeight();
            for (int i = 0; i < weight; i++) {
                try {
                    providerList.add((ServiceInfo) provider.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }

        int MAX_LEN = providerList.size();
        int index = RandomUtils.nextInt(0, MAX_LEN - 1);
        return providerList.get(index);
    }
}
