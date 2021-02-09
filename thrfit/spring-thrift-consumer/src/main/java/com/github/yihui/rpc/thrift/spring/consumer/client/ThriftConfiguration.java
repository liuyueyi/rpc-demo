package com.github.yihui.rpc.thrift.spring.consumer.client;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wuzebang
 * @date 2021/2/9
 */
@Configuration
@EnableConfigurationProperties(ThriftConfig.class)
public class ThriftConfiguration {
    @Bean
    public ThriftClientConnectFactory thriftClientConnectFactory(ThriftConfig thriftConfig) {
        return new ThriftClientConnectFactory(thriftConfig);
    }
}
