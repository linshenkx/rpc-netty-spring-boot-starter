package com.linshen.rpcnettyserverspringbootautoconfigure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 2018/11/1
 * @Description: TODO
 */
@Data
@ConfigurationProperties(prefix = "zk")
public class ZKProperties {
    private List<String> addressList = new ArrayList<>();
    private int sessionTimeOut=5000;
    private int connectTimeOut=1000;
    private String registryPath="/defaultRegistry";

}
