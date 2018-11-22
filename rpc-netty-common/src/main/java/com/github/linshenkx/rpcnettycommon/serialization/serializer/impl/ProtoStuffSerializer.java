package com.github.linshenkx.rpcnettycommon.serialization.serializer.impl;

import com.github.linshenkx.rpcnettycommon.serialization.serializer.ISerializer;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 2018/11/18
 * @Description: protocolbuffer序列化工具(基于protostuff)
 */
public class ProtoStuffSerializer implements ISerializer {

    /**
     * 用于缓存类对象与Schema的对应关系，避免重复创建Schema
     */
    private static final Map<Class<?>, Schema<?>> CACHED_SCHEMA = new ConcurrentHashMap<>();

    /**
     * 用于高效便捷地生成类实例，而无需构造方法支持
     */
    private static final Objenesis OBJENESIS = new ObjenesisStd(true);

    /**
     * 序列化（对象 -> 字节数组）
     */
    @Override
    @SuppressWarnings("unchecked")
    public  <T> byte[] serialize(T obj) {
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化（字节数组 -> 对象）
     */
    @Override
    public  <T> T deserialize(byte[] data, Class<T> cls) {
        try {
            T message = OBJENESIS.newInstance(cls);
            Schema<T> schema = getSchema(cls);
            ProtostuffIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private  <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) CACHED_SCHEMA.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            CACHED_SCHEMA.put(cls, schema);
        }
        return schema;
    }
}
