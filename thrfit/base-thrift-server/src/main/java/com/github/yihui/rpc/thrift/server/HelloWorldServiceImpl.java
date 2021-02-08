package com.github.yihui.rpc.thrift.server;

import com.github.yihui.rpc.thrfit.api.Greeting;
import com.github.yihui.rpc.thrfit.api.HelloWorldService;
import com.github.yihui.rpc.thrfit.api.Person;

/**
 * @author wuzebang
 * @date 2021/2/8
 */
public class HelloWorldServiceImpl implements HelloWorldService.Iface{
    @Override
    public Greeting sayHello(Person person) {
        System.out.println("receive person: " + person);
        Greeting greeting = new Greeting();
        greeting.setMessage("response for " + person);
        return greeting;
    }
}
