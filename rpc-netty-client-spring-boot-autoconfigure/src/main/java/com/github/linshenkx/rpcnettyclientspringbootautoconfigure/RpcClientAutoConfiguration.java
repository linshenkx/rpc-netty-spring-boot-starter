package com.github.linshenkx.rpcnettyclientspringbootautoconfigure;


import com.github.linshenkx.rpcnettyclientspringbootautoconfigure.processor.RpcClientBeanPostProcessor;
import com.github.linshenkx.rpcnettyclientspringbootautoconfigure.client.RpcClient;
import com.github.linshenkx.rpcnettyclientspringbootautoconfigure.discovery.zookeeper.ZKServiceDiscovery;
import com.github.linshenkx.rpcnettyclientspringbootautoconfigure.properties.RpcClientProperties;
import com.github.linshenkx.rpcnettycommon.properties.ZKProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 2018/11/2
 * @Description: TODO
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnClass(RpcClient.class)
public class RpcClientAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public RpcClientProperties defaultRpcServerProperties(){
        return new RpcClientProperties();
    }

    @ConditionalOnMissingBean
    @Bean
    public ZKProperties defaultZKProperties(){
        return new ZKProperties();
    }

    @ConditionalOnMissingBean
    @Bean
    public ZKServiceDiscovery zkServiceDiscovery(){
        return new ZKServiceDiscovery();
    }

    @Bean
    public RpcClient rpcClient(){
        return new RpcClient();
    }

    @Bean
    public RpcClientBeanPostProcessor rpcClientBeanPostProcessor(){
        return new RpcClientBeanPostProcessor();
    }
}
