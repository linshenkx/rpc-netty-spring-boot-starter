package com.github.linshenkx.rpcnettyserverspringbootautoconfigure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 2018/11/1
 * @Description: TODO
 */
@Data
@ConfigurationProperties(prefix = "rpc.server")
public class RpcServerProperties {
    private int port=9000;
}
