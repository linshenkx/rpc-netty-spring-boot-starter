package com.github.linshenkx.rpcnettycommon.codec.encode;

import com.github.linshenkx.rpcnettycommon.bean.RemotingTransporter;
import com.github.linshenkx.rpcnettycommon.protocal.XuanProtocol;
import com.github.linshenkx.rpcnettycommon.serialization.common.SerializeType;
import com.github.linshenkx.rpcnettycommon.serialization.engine.SerializerEngine;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-12
 * @Description: TODO
 */
public class RemotingTransporterEncoder extends MessageToByteEncoder<RemotingTransporter> {

    //序列化类型
    private SerializeType serializeType;

    public RemotingTransporterEncoder(SerializeType serializeType){
        this.serializeType=serializeType;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RemotingTransporter remotingTransporter, ByteBuf byteBuf) throws Exception {
        //获取请求体数组
        //使用序列化引擎
        byte[] body= SerializerEngine.serialize(remotingTransporter.getBodyContent(), serializeType.getSerializeType());
        //magic+flag+invokeId+bodyLength+bodyContent
        byteBuf.writeShort(XuanProtocol.MAGIC)
                .writeByte(remotingTransporter.getFlag())
                .writeLong(remotingTransporter.getInvokeId())
                .writeInt(body.length)
                .writeBytes(body);

    }
}
