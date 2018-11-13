package com.github.linshenkx.rpcnettycommon.bean;

import lombok.Builder;
import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-12
 * @Description: 自定义远程传输实体 (magic+flag+invokeId+bodyLength+bodyContent)
 */
@Data
@Builder
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
    private byte flag;


    /**
     * 每一个请求的唯一识别id（由于采用异步通讯的方式，用来把请求request和返回的response对应上）
     */
    private long invokeId;

    /**
     * 消息体字节数组长度
     */
    private int bodyLength;

    /**
     * 消息体内容(还需要编码序列化成字节数组)
     */
    private transient BodyContent bodyContent;

    /**
     * 默认构造自增获得invokeId
     */
    public RemotingTransporter(){
        // getAndIncrement() When it grows to MAX_VALUE, it will grow to MIN_VALUE, and the negative can be used as ID
        invokeId=invokeIdGnerator.getAndIncrement();
    }

    /**
     * 自定义invokeId,用于还原RemotingTransporter
     * @param invokeId
     */
    public RemotingTransporter(Long invokeId){
        this.invokeId=invokeId;
    }


}
