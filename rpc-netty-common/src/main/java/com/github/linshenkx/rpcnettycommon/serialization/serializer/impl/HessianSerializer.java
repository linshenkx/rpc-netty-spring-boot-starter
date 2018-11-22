package com.github.linshenkx.rpcnettycommon.serialization.serializer.impl;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.github.linshenkx.rpcnettycommon.serialization.serializer.ISerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-18
 * @Description: Hessian序列化(需有Serializable接口)
 */
public class HessianSerializer implements ISerializer {


    @Override
    public byte[] serialize(Object obj) {
        try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            HessianOutput ho = new HessianOutput(os);
            ho.writeObject(obj);
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {

        try(ByteArrayInputStream is = new ByteArrayInputStream(data)) {
            HessianInput hi = new HessianInput(is);
            return (T) hi.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
