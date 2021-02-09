package com.github.yihui.rpc.thrift.spring.consumer.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author wuzebang
 * @date 2021/2/9
 */
@Data
@ConfigurationProperties(prefix = "thrift")
public class ThriftConfig {
    private String host;
    private Integer port;

    private Integer minThreadPool;

    private Integer maxThreadPool;

    private Boolean multi;
}
