package com.github.linshenkx.rpcnettycommon.protocal.xuan;

import com.github.linshenkx.rpcnettycommon.bean.BodyContent;
import lombok.*;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-12
 * @Description: 自定义远程传输实体 (magic+flag+invokeId+bodyLength+bodyContent)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RemotingTransporter {

    /**
     * 用原子递增的方式来获取不重复invokeId
     */
    private static final AtomicLong invokeIdGnerator=new AtomicLong(0L);


    /**
     * 标志位, 一共8个地址位。
     * 低四位用来表示消息体数据用的序列化工具的类型
     * 高四位中，第一位为1表示是request请求，为0表示是reponse应答
     * TODO:第二位为1表示双向传输（即有返回response）
     * TODO:第三位为1表示是心跳ping事件
     * TODO:预留位
     */
    private Flag flag;

    @Getter
    @ToString
    public static class Flag{
        private boolean isRequest;
        private boolean isTwoway;
        private boolean isPing;
        private boolean isOther;

        private int serializeType;

        private byte thisByte;


        public Flag(boolean isRequest, boolean isTwoway, boolean isPing, boolean isOther, int serializeType) {

            if(serializeType<0||serializeType>15){
                throw new IllegalArgumentException("serializeType 对应整数应该在 0 到 15 之间");
            }

            this.isRequest = isRequest;
            this.isTwoway = isTwoway;
            this.isPing = isPing;
            this.isOther = isOther;
            this.serializeType = serializeType;

            int byteTem= (isRequest?1:0)<<7;
            byteTem=byteTem | ((isTwoway?1:0)<<6);
            byteTem=byteTem | ((isPing?1:0)<<5);
            byteTem=byteTem | ((isOther?1:0)<<4);
            byteTem=byteTem | serializeType;

            this.thisByte= (byte) byteTem;
        }

        public Flag(byte thisByte){
            this.thisByte=thisByte;

            isRequest=((thisByte>>>7)&1)==1;
            isTwoway=((thisByte>>>6)&1)==1;
            isPing=((thisByte>>>5)&1)==1;
            isOther=((thisByte>>>4)&1)==1;

            serializeType=thisByte & 15;

        }

    }


    /**
     * 每一个请求的唯一识别id（由于采用异步通讯的方式，用来把请求request和返回的response对应上）
     */
    private long invokeId=invokeIdGnerator.getAndIncrement();

    /**
     * 消息体字节数组长度
     */
    private int bodyLength;

    /**
     * 消息体内容(还需要编码序列化成字节数组)
     */
    private transient BodyContent bodyContent;


}
