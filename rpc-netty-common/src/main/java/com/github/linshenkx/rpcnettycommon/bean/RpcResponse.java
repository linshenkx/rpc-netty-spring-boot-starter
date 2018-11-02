package com.github.linshenkx.rpcnettycommon.bean;

import lombok.Data;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 2018/10/31
 * @Description: TODO
 */
@Data
public class RpcResponse {
    /**
     * 对应请求的requestId
     */
    private String requestId;
    /**
     * 异常信息
     */
    private Exception exception;
    /**
     * 响应结果
     */
    private Object result;
}
