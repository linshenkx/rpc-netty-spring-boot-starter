package com.github.linshenkx.rpcnettycommon.bean;

import com.github.linshenkx.rpcnettycommon.route.WeightGetAble;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-13
 * @Description: 服务信息,用于存储到注册中心
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceInfo implements Cloneable , WeightGetAble {

    private String host;
    private int port;
    /**
     * 权重信息
     */
    private int weight;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public int getWeightFactors() {
        return getWeight();
    }
}
