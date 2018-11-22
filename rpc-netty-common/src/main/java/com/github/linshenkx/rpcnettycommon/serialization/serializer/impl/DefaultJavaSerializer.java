package com.github.linshenkx.rpcnettycommon.serialization.serializer.impl;


import com.github.linshenkx.rpcnettycommon.serialization.serializer.ISerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-18
 * @Description: Java默认序列化(需有Serializable接口)
 */
public class DefaultJavaSerializer implements ISerializer {


    @Override
    public <T> byte[] serialize(T obj) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)){
            objectOutputStream.writeObject(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }


    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (T) objectInputStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
