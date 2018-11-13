package com.github.linshenkx.rpcnettycommon.cluster;


import org.apache.commons.lang3.StringUtils;

/**
 * @author liyebing created on 17/4/23.
 * @version $Id$
 */
public enum ClusterStrategyEnum {

    //随机算法
    Random("Random"),
    //权重随机算法
    WeightRandom("WeightRandom"),
    //轮询算法
    Polling("Polling"),
    //权重轮询算法
    WeightPolling("WeightPolling"),
    //源地址hash算法
    Hash("Hash");

    private ClusterStrategyEnum(String code) {
        this.code = code;
    }


    public static ClusterStrategyEnum queryByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (ClusterStrategyEnum strategy : values()) {
            if (StringUtils.equals(code, strategy.getCode())) {
                return strategy;
            }
        }
        return null;
    }

    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
