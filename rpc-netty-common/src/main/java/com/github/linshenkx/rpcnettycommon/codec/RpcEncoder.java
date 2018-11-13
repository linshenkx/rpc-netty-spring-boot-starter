package com.github.linshenkx.rpcnettycommon.codec;

import com.github.linshenkx.rpcnettycommon.serialization.common.SerializeType;
import com.github.linshenkx.rpcnettycommon.serialization.engine.SerializerEngine;
import com.github.linshenkx.rpcnettycommon.serialization.serializer.impl.ProtoStuffSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


/**
 * @version V1.0
 * @author: lin_shen
 * @date: 2018/10/31
 * @Description: RPC编码器
 */

public class RpcEncoder extends MessageToByteEncoder {

    private Class<?> genericClass;

    public RpcEncoder(Class<?> genericClass){
        this.genericClass=genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if(genericClass.isInstance(msg)){
            byte[] data= SerializerEngine.serialize(msg, SerializeType.ProtoStuffSerializer.getSerializeType());
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}
