package com.github.linshenkx.rpcnettycommon.serialization.serializer.impl;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.linshenkx.rpcnettycommon.serialization.serializer.ISerializer;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-18
 * @Description: Json序列化（基于jackson实现）
 */
public class JSONSerializer implements ISerializer {


    /**
     * ObjectMapper可配置json序列化规则，该类的创建需要消耗较多资源，故应配置为类成员对象
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();



    static {
        //允许字段名不带引号（这不符合JSON标准，但在JS中合法）
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        //允许使用单引号代替双引号（这不符合JSON标准，但这一些JSON生成器中合法）
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        //允许携带不加引号的控制字符（即ASCII码小于32的），不符合JSON标准，故默认为false
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        //允许出现未定义处理方法（没有对应的setter方法或其他的处理器）的未知字段
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

    @Override
    public <T> byte[] serialize(T obj) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(obj);
            return json.getBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        String json = new String(data);
        try {
            return (T) OBJECT_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
