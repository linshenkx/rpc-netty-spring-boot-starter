package com.github.linshenkx.rpcnettycommon.serialization;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-18
 * @Description: 序列化枚举类
 */
public enum SerializeTypeEnum {
    /**
     * Java默认序列化
     */
    DefaultJava(0),
    /**
     * Hessian序列化
     */
    Hessian(1),
    /**
     * Json序列化（基于Jackson）
     */
    JSON(2),
    /**
     * Protostuff序列化
     */
    ProtoStuff(3),
    /**
     * Xml序列化
     */
    Xml(4),


    /**
     * Avro序列化，需借助IDL
     */
    Avro(5),
    /**
     * ProtocolBuffer序列化，需借助IDL
     */
    ProtocolBuffer(6),
    /**
     * Thrift序列化，需借助IDL
     */
    Thrift(7);

    private int code;

    SerializeTypeEnum(int code) {
        this.code = code;
    }

    public static SerializeTypeEnum queryByCode  (int code) {
        for (SerializeTypeEnum type : values()) {
            if(type.getCode()==code){
                return type;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

}
