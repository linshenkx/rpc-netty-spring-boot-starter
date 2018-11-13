package com.github.linshenkx.rpcnettycommon.cluster;

import com.github.linshenkx.rpcnettycommon.bean.ServiceInfo;

import java.util.List;

/**
 * @author liyebing created on 17/2/12.
 * @version $Id$
 */
public interface ClusterStrategy {

    /**
     * 负载策略算法
     *
     * @param providerServices
     * @return
     */
    public ServiceInfo select(List<ServiceInfo> providerServices);
}
