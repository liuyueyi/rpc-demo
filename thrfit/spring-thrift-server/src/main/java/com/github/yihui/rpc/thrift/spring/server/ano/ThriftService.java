package com.github.yihui.rpc.thrift.spring.server.ano;

import org.apache.thrift.TProcessor;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * @author wuzebang
 * @date 2021/2/9
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface ThriftService {
    Class<? extends TProcessor> processor();
}
