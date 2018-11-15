package com.github.linshenkx.rpcnettycommon.route.impl;


import com.github.linshenkx.rpcnettycommon.route.RouteStrategy;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-14
 * @Description: 轮询策略 负载均衡
 */
public class PollingRouteStrategyImpl implements RouteStrategy {

    /**
     * 计数器
     */
    private int index = 0;
    private Lock lock = new ReentrantLock();

    @Override
    public <T> T select(List<T> primeList) {
        T point=null;
        try {
            lock.tryLock(10,TimeUnit.MILLISECONDS);
            //若计数大于列表元素个数,将计数器归0
            if (index >= primeList.size()) {
                index = 0;
            }
            point=primeList.get(index++);
        }catch (InterruptedException e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
        //保证程序健壮性,若未取到服务,则改用随机算法
        if (point == null) {
            point = primeList.get(RandomUtils.nextInt(0, primeList.size()));
        }
        return point;
    }

}
