package com.github.linshenkx.rpcnettyclientspringbootautoconfigure.properties;

import com.github.linshenkx.rpcnettycommon.route.RouteStrategyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 2018/11/1
 * @Description: TODO
 */
@Data
@ConfigurationProperties(prefix = "rpc.client")
public class RpcClientProperties {
    private RouteStrategyEnum routeStrategy= RouteStrategyEnum.Random;
}
