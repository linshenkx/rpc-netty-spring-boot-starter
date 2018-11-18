package com.github.linshenkx.rpcnettycommon.serialization;


import avro.shaded.com.google.common.collect.Maps;
import com.github.linshenkx.rpcnettycommon.serialization.serializer.ISerializer;
import com.github.linshenkx.rpcnettycommon.serialization.serializer.impl.*;

import java.util.Map;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-18
 * @Description: 序列化引擎
 */
public class SerializerEngine {

    private static final Map<SerializeTypeEnum, ISerializer> SERIALIZER_MAP = Maps.newConcurrentMap();

    static {
        SERIALIZER_MAP.put(SerializeTypeEnum.DefaultJava, new DefaultJavaSerializer());
        SERIALIZER_MAP.put(SerializeTypeEnum.Hessian, new HessianSerializer());
        SERIALIZER_MAP.put(SerializeTypeEnum.JSON, new JSONSerializer());
        SERIALIZER_MAP.put(SerializeTypeEnum.Xml, new XmlSerializer());
        SERIALIZER_MAP.put(SerializeTypeEnum.ProtoStuff, new ProtoStuffSerializer());

        //以下三类不能使用普通的java bean，需借助IDL
        SERIALIZER_MAP.put(SerializeTypeEnum.Avro, new AvroSerializer());
        SERIALIZER_MAP.put(SerializeTypeEnum.Thrift, new ThriftSerializer());
        SERIALIZER_MAP.put(SerializeTypeEnum.ProtocolBuffer, new ProtocolBufferSerializer());
    }

    public static <T> byte[] serialize(T obj, SerializeTypeEnum serializeTypeEnum) {

        ISerializer serializer = SERIALIZER_MAP.get(serializeTypeEnum);
        return serializer.serialize(obj);
    }

    public static <T> T deserialize(byte[] data, Class<T> clazz, SerializeTypeEnum serializeTypeEnum) {

        ISerializer serializer = SERIALIZER_MAP.get(serializeTypeEnum);
        return serializer.deserialize(data, clazz);

    }

}
