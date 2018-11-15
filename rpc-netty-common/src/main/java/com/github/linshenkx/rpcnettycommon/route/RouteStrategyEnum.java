package com.github.linshenkx.rpcnettycommon.route;


/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-14
 * @Description: 路由策略 枚举类
 */
public enum RouteStrategyEnum {

    //随机算法
    Random(0),
    //轮询算法
    Polling(1),
    //源地址hash算法
    HashIP(2);


    private int code;

    RouteStrategyEnum(int code) {
        this.code = code;
    }

    public static RouteStrategyEnum queryByCode  (int code) {
        for (RouteStrategyEnum strategy : values()) {
            if(strategy.getCode()==code){
                return strategy;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }
}
