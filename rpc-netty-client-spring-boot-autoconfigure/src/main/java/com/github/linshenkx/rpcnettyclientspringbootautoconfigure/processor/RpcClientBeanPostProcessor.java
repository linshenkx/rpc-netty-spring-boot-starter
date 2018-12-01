package com.github.linshenkx.rpcnettyclientspringbootautoconfigure.processor;

import com.github.linshenkx.rpcnettyclientspringbootautoconfigure.client.RpcClient;
import com.github.linshenkx.rpcnettycommon.annotation.RpcReference;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-12-1
 * @Description: 在 Bean 完成实例化后增加自己的处理逻辑
 */
@Component
@Log4j2
public class RpcClientBeanPostProcessor implements BeanPostProcessor {

    @Autowired
    private RpcClient rpcClient;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        processRpcReference(bean);

        return bean;
    }

    private void processRpcReference(Object bean) {
        Class beanClass = bean.getClass();
        do {
            Field[] fields = beanClass.getDeclaredFields();
            for (Field field : fields) {
                if(field.getAnnotation(RpcReference.class)!=null){
                    field.setAccessible(true);
                    try {
                        field.set(bean, rpcClient.create(field.getType()));
                    } catch (IllegalAccessException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        } while ((beanClass = beanClass.getSuperclass()) != null);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}
