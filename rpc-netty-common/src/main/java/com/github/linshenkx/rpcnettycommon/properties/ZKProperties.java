package com.github.linshenkx.rpcnettycommon.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 2018/11/1
 * @Description: TODO
 */
@Data
@ConfigurationProperties(prefix = "zk")
public class ZKProperties {
    private String address ;
    private int sessionTimeOut=5000;
    private int connectTimeOut=1000;
    private String registryPath="/defaultRegistry";

}
