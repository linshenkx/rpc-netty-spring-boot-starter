package com.linshen.rpcnettyserverspringbootautoconfigure;


import com.linshen.rpcnettyserverspringbootautoconfigure.properties.RpcServerProperties;
import com.linshen.rpcnettyserverspringbootautoconfigure.properties.ZKProperties;
import com.linshen.rpcnettyserverspringbootautoconfigure.registry.ZKServiceRegistry;
import com.linshen.rpcnettyserverspringbootautoconfigure.server.RpcServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 2018/11/2
 * @Description: TODO
 */
@Configuration
@ConditionalOnClass(RpcServer.class)
public class RpcServerAutoConfiguration {
    @ConditionalOnMissingBean
    @Bean
    public RpcServerProperties defaultRpcServerProperties(){
        return new RpcServerProperties();
    }

    @ConditionalOnMissingBean
    @Bean
    public ZKProperties defaultZKProperties(){
        return new ZKProperties();
    }

    @ConditionalOnMissingBean
    @Bean
    public ZKServiceRegistry zkServiceRegistry(){
        return new ZKServiceRegistry();
    }

    @Bean
    public RpcServer rpcServer(){
        return new RpcServer();
    }



}
