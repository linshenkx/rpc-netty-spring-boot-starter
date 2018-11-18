package com.github.linshenkx.rpcnettycommon.codec.decode;

import com.github.linshenkx.rpcnettycommon.bean.BodyContent;
import com.github.linshenkx.rpcnettycommon.bean.RpcRequest;
import com.github.linshenkx.rpcnettycommon.bean.RpcResponse;
import com.github.linshenkx.rpcnettycommon.exception.remoting.RemotingContextException;
import com.github.linshenkx.rpcnettycommon.protocal.xuan.RemotingTransporter;
import com.github.linshenkx.rpcnettycommon.protocal.xuan.XuanProtocol;
import com.github.linshenkx.rpcnettycommon.serialization.SerializeTypeEnum;
import com.github.linshenkx.rpcnettycommon.serialization.SerializerEngine;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.log4j.Log4j2;

import java.util.List;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-12
 * @Description: TODO
 */
@Log4j2
public class RemotingTransporterDecoder extends ReplayingDecoder<RemotingTransporterDecoder.State> {

    private static final int MAX_BODY_SIZE = 1024 * 1024 * 5;

    /**
     * 用于暂存解码RemotingTransporter信息,一个就够了
     */
    private final RemotingTransporter remotingTransporter=RemotingTransporter.builder().build();

    /**
     * 用于ReplayingDecoder的状态管理
     */
    enum State {
        HEADER_MAGIC, HEADER_FLAG, HEADER_INVOKE_ID, HEADER_BODY_LENGTH, BODY
    }

    public RemotingTransporterDecoder( ){
        //设置 state() 的初始值,以便进入switch
        super(State.HEADER_MAGIC);
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
                remotingTransporter.setFlag(new RemotingTransporter.Flag(byteBuf.readByte()));
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
                Class genericClass=remotingTransporter.getFlag().isRequest()?RpcRequest.class: RpcResponse.class;
                BodyContent bodyContent= (BodyContent) SerializerEngine.deserialize(bytes,genericClass,SerializeTypeEnum.queryByCode(remotingTransporter.getFlag().getSerializeType()));
                RemotingTransporter remotingTransporter1=RemotingTransporter.builder()
                        .flag(remotingTransporter.getFlag())
                        .invokeId(remotingTransporter.getInvokeId())
                        .bodyLength(remotingTransporter.getBodyLength())
                        .bodyContent(bodyContent)
                        .build();
                list.add(remotingTransporter1);
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
            log.error("魔数不匹配");
            throw new RemotingContextException("magic value is not equal "+XuanProtocol.MAGIC);
        }
    }



}
