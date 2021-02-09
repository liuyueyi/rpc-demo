package com.github.yihui.rpc.thrift.spring.consumer.ano;

import java.lang.annotation.*;

/**
 * @author wuzebang
 * @date 2021/2/9
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ThriftClient {
}
