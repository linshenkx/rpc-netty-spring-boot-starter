package com.github.linshenkx.rpcnettycommon.route.impl;


import com.github.linshenkx.rpcnettycommon.route.RouteStrategy;
import org.apache.commons.lang3.RandomUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-14
 * @Description: 基于本地IP的哈希策略 负载均衡
 */
public class HashIPRouteStrategyImpl implements RouteStrategy {

    @Override
    public <T> T select(List<T> primeList) {
        String localIP=null;
        try {
            localIP=InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        //保证程序健壮性,若未取到域名,则采用改用随机字符串
        if(localIP==null){
            localIP= RandomUtils.nextBytes(5).toString();
        }

        //获取源地址对应的hashcode
        int hashCode = localIP.hashCode();
        //获取服务列表大小
        int size = primeList.size();

        return primeList.get(Math.abs(hashCode) % size);
    }

}
