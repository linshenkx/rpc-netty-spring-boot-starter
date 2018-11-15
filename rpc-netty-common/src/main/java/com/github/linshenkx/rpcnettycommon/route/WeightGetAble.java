package com.github.linshenkx.rpcnettycommon.route;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-15
 * @Description: 权重接口，使用权重负载均衡的列表元素必须实现此接口
 */

public interface WeightGetAble {

    default int getWeightFactors(){
        return 1;
    }

}
