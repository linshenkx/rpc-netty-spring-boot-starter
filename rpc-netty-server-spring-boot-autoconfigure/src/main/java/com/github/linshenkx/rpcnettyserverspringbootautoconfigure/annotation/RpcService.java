package com.github.linshenkx.rpcnettyserverspringbootautoconfigure.annotation;

import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 2018/10/31
 * @Description:
 * RPC服务注解（标注在rpc服务实现类上）
 * 使用@Service注解使被@RpcService标注的类都能被Spring管理
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface RpcService {
    Class<?> value();
}
