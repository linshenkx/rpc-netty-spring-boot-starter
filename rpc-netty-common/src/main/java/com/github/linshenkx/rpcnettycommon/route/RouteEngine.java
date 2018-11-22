package com.github.linshenkx.rpcnettycommon.route;


import com.github.linshenkx.rpcnettycommon.route.impl.HashIPRouteStrategyImpl;
import com.github.linshenkx.rpcnettycommon.route.impl.PollingRouteStrategyImpl;
import com.github.linshenkx.rpcnettycommon.route.impl.RandomRouteStrategyImpl;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-12
 * @Description: 路由均衡引擎
 */
public class RouteEngine {
    
    private final static RouteStrategy RANDOM_ROUTE_STRATEGY_IMPL =new RandomRouteStrategyImpl();
    
    private final static RouteStrategy HASHIP_ROUTE_STRATEGY_IMPL =new HashIPRouteStrategyImpl();

    public static RouteStrategy queryClusterStrategy(int clusterStrategyCode) {
        RouteStrategyEnum clusterStrategyEnum = RouteStrategyEnum.queryByCode(clusterStrategyCode);
        return queryClusterStrategy(clusterStrategyEnum);
    }

    public static RouteStrategy queryClusterStrategy(RouteStrategyEnum routeStrategyEnum) {
        if(routeStrategyEnum==null){
            return new RandomRouteStrategyImpl();
        }
        switch (routeStrategyEnum){
            case Random:
                return RANDOM_ROUTE_STRATEGY_IMPL ;
            case Polling:
                return new PollingRouteStrategyImpl();
            case HashIP:
                return HASHIP_ROUTE_STRATEGY_IMPL ;
            default:
                return RANDOM_ROUTE_STRATEGY_IMPL ;
        }
    }

}
