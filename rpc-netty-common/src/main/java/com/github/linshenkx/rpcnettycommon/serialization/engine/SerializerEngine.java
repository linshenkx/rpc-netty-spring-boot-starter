package com.github.linshenkx.rpcnettycommon.serialization.engine;


import avro.shaded.com.google.common.collect.Maps;
import com.github.linshenkx.rpcnettycommon.serialization.common.SerializeType;
import com.github.linshenkx.rpcnettycommon.serialization.serializer.ISerializer;
import com.github.linshenkx.rpcnettycommon.serialization.serializer.impl.*;

import java.util.Map;

/**
 * @author liyebing created on 17/1/23.
 * @version $Id$
 */
public class SerializerEngine {

    public static final Map<SerializeType, ISerializer> serializerMap = Maps.newConcurrentMap();

    static {
        serializerMap.put(SerializeType.DefaultJavaSerializer, new DefaultJavaSerializer());
        serializerMap.put(SerializeType.HessianSerializer, new HessianSerializer());
        serializerMap.put(SerializeType.JSONSerializer, new JSONSerializer());
        serializerMap.put(SerializeType.XmlSerializer, new XmlSerializer());
        serializerMap.put(SerializeType.ProtoStuffSerializer, new ProtoStuffSerializer());
        serializerMap.put(SerializeType.MarshallingSerializer, new MarshallingSerializer());

        //以下三类不能使用普通的java bean
        serializerMap.put(SerializeType.AvroSerializer, new AvroSerializer());
        serializerMap.put(SerializeType.ThriftSerializer, new ThriftSerializer());
        serializerMap.put(SerializeType.ProtocolBufferSerializer, new ProtocolBufferSerializer());
    }


    public static <T> byte[] serialize(T obj, String serializeType) {
        SerializeType serialize = SerializeType.queryByType(serializeType);
        if (serialize == null) {
            throw new RuntimeException("serialize is null");
        }

        ISerializer serializer = serializerMap.get(serialize);
        if (serializer == null) {
            throw new RuntimeException("serialize error");
        }

        try {
            return serializer.serialize(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static <T> T deserialize(byte[] data, Class<T> clazz, String serializeType) {

        SerializeType serialize = SerializeType.queryByType(serializeType);
        if (serialize == null) {
            throw new RuntimeException("serialize is null");
        }
        ISerializer serializer = serializerMap.get(serialize);
        if (serializer == null) {
            throw new RuntimeException("serialize error");
        }

        try {
            return serializer.deserialize(data, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
