package com.github.yihui.rpc.thrift.spring.server.impl;

import com.github.yihui.rpc.thrfit.api.Greeting;
import com.github.yihui.rpc.thrfit.api.HelloWorldService;
import com.github.yihui.rpc.thrfit.api.Person;
import com.github.yihui.rpc.thrift.spring.server.ano.ThriftService;
import org.apache.thrift.TException;

/**
 * @author wuzebang
 * @date 2021/2/9
 */
@ThriftService(processor = HelloWorldService.Processor.class)
public class HelloWorldServiceImpl implements HelloWorldService.Iface {

    @Override
    public Greeting sayHello(Person person) throws TException {
        System.out.println("thrift server receive: " + person);
        return new Greeting("receive: " + person);
    }
}
