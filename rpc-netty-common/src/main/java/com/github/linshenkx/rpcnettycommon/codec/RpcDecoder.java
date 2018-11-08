package com.github.linshenkx.rpcnettycommon.codec;

import com.github.linshenkx.rpcnettycommon.util.ProtoSerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 2018/10/31
 * @Description: Rpc解码器
 */

public class RpcDecoder extends ByteToMessageDecoder {
    private Class<?> genericClass;

    public RpcDecoder(Class<?> genericClass){
        this.genericClass=genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes()<4){
            return;
        }
        in.markReaderIndex();
        int dataLength=in.readInt();
        if(in.readableBytes()<dataLength){
            in.resetReaderIndex();
            return;
        }
        byte[] data=new byte[dataLength];
        in.readBytes(data);
        out.add(ProtoSerializationUtil.deserialize(data,genericClass));
    }
}
