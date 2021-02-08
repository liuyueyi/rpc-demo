package com.github.yihui.rpc.dubbo.consumer;

import com.github.yihui.rpc.dubbo.api.Greeting;
import com.github.yihui.rpc.dubbo.api.HelloWorldServiceApi;
import com.github.yihui.rpc.dubbo.api.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author wuzebang
 * @date 2021/2/8
 */
@Component
public class HelloWorldProvider {
    @Autowired
    private HelloWorldServiceApi helloWorldServiceApi;

    public void call() {
        Greeting greeting = helloWorldServiceApi.sayHello(new Person("Yi", "Hui"));
        System.out.println(greeting);
    }
}
