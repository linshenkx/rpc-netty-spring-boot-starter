package com.github.linshenkx.rpcnettycommon.codec.decode;

import com.github.linshenkx.rpcnettycommon.bean.BodyContent;
import com.github.linshenkx.rpcnettycommon.bean.RemotingTransporter;
import com.github.linshenkx.rpcnettycommon.exception.remoting.RemotingContextException;
import com.github.linshenkx.rpcnettycommon.protocal.XuanProtocol;
import com.github.linshenkx.rpcnettycommon.serialization.common.SerializeType;
import com.github.linshenkx.rpcnettycommon.serialization.engine.SerializerEngine;
import com.github.linshenkx.rpcnettycommon.serialization.serializer.impl.ProtoStuffSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-12
 * @Description: TODO
 */
public class RemotingTransporterDecoder extends ReplayingDecoder<RemotingTransporterDecoder.State> {

    private static final Logger logger = LoggerFactory.getLogger(RemotingTransporterDecoder.class);
    private static final int MAX_BODY_SIZE = 1024 * 1024 * 5;
    //解码对象编码所使用序列化类型
    private SerializeType serializeType;
    /**
     * 用于暂存解码RemotingTransporter信息,一个就够了
     */
    private final RemotingTransporter remotingTransporter=new RemotingTransporter();

    /**
     * 用于ReplayingDecoder的状态管理
     */
    enum State {
        HEADER_MAGIC, HEADER_FLAG, HEADER_INVOKE_ID, HEADER_BODY_LENGTH, BODY
    }

    public RemotingTransporterDecoder(SerializeType serializeType){
        //设置 state() 的初始值,以便进入switch
        super(State.HEADER_MAGIC);
        this.serializeType = serializeType;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        //注意这里在BODY之前都没有break
        switch (this.state()){
            case HEADER_MAGIC:
                checkMagic(byteBuf.readShort());
                //移到下一检查点(一是改变state的值的状态，二是获取到最新的读指针的下标)
                checkpoint(State.HEADER_FLAG);
            case HEADER_FLAG:
                remotingTransporter.setFlag(byteBuf.readByte());
                checkpoint(State.HEADER_INVOKE_ID);
            case HEADER_INVOKE_ID:
                remotingTransporter.setInvokeId(byteBuf.readLong());
                checkpoint(State.HEADER_BODY_LENGTH);
            case HEADER_BODY_LENGTH:
                remotingTransporter.setBodyLength(byteBuf.readInt());
                checkpoint(State.HEADER_BODY_LENGTH);
            case BODY:
                int bodyLength = checkBodyLength(remotingTransporter.getBodyLength());
                byte[] bytes=new byte[bodyLength];
                byteBuf.readBytes(bytes);
                BodyContent bodyContent= SerializerEngine.deserialize(bytes,BodyContent.class,serializeType.getSerializeType());
                list.add( RemotingTransporter.builder()
                        .flag(remotingTransporter.getFlag())
                        .invokeId(remotingTransporter.getInvokeId())
                        .bodyLength(remotingTransporter.getBodyLength())
                        .bodyContent(bodyContent)
                        .build()
                );
                break;
            default:
                break;
        }
        //顺利读完body后应置回起点
        checkpoint(State.HEADER_MAGIC);

    }

    private int checkBodyLength(int bodyLength) throws RemotingContextException {
        if (bodyLength > MAX_BODY_SIZE) {
            throw new RemotingContextException("body of request is bigger than limit value "+ MAX_BODY_SIZE);
        }
        return bodyLength;
    }

    private void checkMagic(short magic) throws RemotingContextException{
        //检查魔数
        if (XuanProtocol.MAGIC != magic) {
            logger.error("魔数不匹配");
            throw new RemotingContextException("magic value is not equal "+XuanProtocol.MAGIC);
        }
    }



}
