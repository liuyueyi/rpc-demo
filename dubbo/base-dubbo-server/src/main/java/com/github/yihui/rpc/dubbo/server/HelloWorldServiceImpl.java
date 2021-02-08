package com.github.yihui.rpc.dubbo.server;

import com.github.yihui.rpc.dubbo.api.Greeting;
import com.github.yihui.rpc.dubbo.api.HelloWorldServiceApi;
import com.github.yihui.rpc.dubbo.api.Person;

/**
 * @author wuzebang
 * @date 2021/2/8
 */
public class HelloWorldServiceImpl implements HelloWorldServiceApi {
    @Override
    public Greeting sayHello(Person person) {
        System.out.println("request " + person);
        return new Greeting("hello " + person);
    }
}
