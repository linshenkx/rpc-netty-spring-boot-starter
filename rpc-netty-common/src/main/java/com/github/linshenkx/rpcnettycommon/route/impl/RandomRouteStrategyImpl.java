package com.github.linshenkx.rpcnettycommon.route.impl;

import com.github.linshenkx.rpcnettycommon.route.RouteStrategy;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;


/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-14
 * @Description: 随机策略 负载均衡
 */
public class RandomRouteStrategyImpl implements RouteStrategy  {

    @Override
    public <T> T select(List<T> primeList) {
        return primeList.get(RandomUtils.nextInt(0, primeList.size()));
    }

}
