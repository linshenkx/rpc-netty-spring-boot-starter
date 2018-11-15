package com.github.linshenkx.rpcnettycommon.route;

import avro.shaded.com.google.common.collect.Lists;

import java.util.List;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-15
 * @Description: TODO
 */

public enum WeightUtil {
    INSTANCE;
    public static  <T extends WeightGetAble> List<T> getWeightList(List<T> primeList){
        //存放加权后列表
        List<T> weightList= Lists.newArrayList();
        for (T prime:primeList){
            //按权重转化为次数添加进加权后列表
            //TODO:需注意权重代表列表长度，故不可过大，此处应优化
            int weight=prime.getWeightFactors();
            for(int i=0;i<weight;i++){
                weightList.add(prime);
            }
        }
        return weightList;
    }
}
