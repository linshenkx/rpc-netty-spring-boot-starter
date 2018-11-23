package com.github.linshenkx.rpcnettycommon.codec.encode;

import com.github.linshenkx.rpcnettycommon.protocal.xuan.RemotingTransporter;
import com.github.linshenkx.rpcnettycommon.serialization.SerializeTypeEnum;
import com.github.linshenkx.rpcnettycommon.serialization.SerializerEngine;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.log4j.Log4j2;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-12
 * @Description: TODO
 */
@Log4j2
public class RemotingTransporterEncoder extends MessageToByteEncoder<RemotingTransporter> {


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RemotingTransporter remotingTransporter, ByteBuf byteBuf) throws Exception {
        //获取请求体数组
        //使用序列化引擎
        byte[] body= SerializerEngine.serialize(remotingTransporter.getBodyContent(), SerializeTypeEnum.queryByCode(remotingTransporter.getFlag().getSerializeType()));
        //magic+flag+invokeId+bodyLength+bodyContent
        byteBuf.writeShort(RemotingTransporter.MAGIC)
                .writeByte(remotingTransporter.getFlag().getThisByte())
                .writeLong(remotingTransporter.getInvokeId())
                .writeInt(body.length)
                .writeBytes(body);
        log.info("write end");

    }


}
