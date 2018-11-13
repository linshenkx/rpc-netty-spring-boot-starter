package com.github.linshenkx.rpcnettycommon.cluster.impl;

import com.github.linshenkx.rpcnettycommon.bean.ServiceInfo;
import com.github.linshenkx.rpcnettycommon.cluster.ClusterStrategy;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;


/**
 * 软负载随机算法实现
 *
 * @author liyebing created on 17/2/12.
 * @version $Id$
 */
public class RandomClusterStrategyImpl implements ClusterStrategy {
    @Override
    public ServiceInfo select(List<ServiceInfo> providerServices) {
        int MAX_LEN = providerServices.size();
        int index = RandomUtils.nextInt(0, MAX_LEN - 1);
        return providerServices.get(index);
    }

}
