package com.linshen.rpcnettycommon.bean;

import lombok.Data;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 2018/10/31
 * @Description: TODO
 */
@Data
public class RpcRequest {
    /**
     * 请求ID
     */
    private String requestId;
    /**
     * 接口名称
     */
    private String interfaceName;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 方法参数类型列表
     */
    private Class<?>[] parameterTypes;
    /**
     * 参数列表
     */
    private Object[] parameters;
}
