package com.github.linshenkx.rpcnettycommon.protocal;

import lombok.Data;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-12
 * @Description: 自定义协议信息
 */
public enum  XuanProtocol {
    /**
     * 使用enum来限制单例使用
     */
    Instance;

    /**
     * 协议头长度
     */
    public static final int HEAD_LENGTH=16;

    /**
     * 魔数
     */
    public static final short MAGIC=(short)0x9826;




}
