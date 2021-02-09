package com.github.yihui.rpc.thrift.spring.consumer.rest;

import com.github.yihui.rpc.thrfit.api.DemoService;
import com.github.yihui.rpc.thrfit.api.Greeting;
import com.github.yihui.rpc.thrfit.api.HelloWorldService;
import com.github.yihui.rpc.thrfit.api.Person;
import com.github.yihui.rpc.thrift.spring.consumer.ano.ThriftClient;
import org.apache.thrift.TException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author wuzebang
 * @date 2021/2/9
 */
@RestController
public class TestConsumer {
    @ThriftClient
    private DemoService.Client demoService;
    @ThriftClient
    private HelloWorldService.Client helloWorldService;

    @GetMapping(path = "test")
    public String test() throws TException {
        int ans = demoService.calculate(10, (int) (Math.random() * 100));
        System.out.println("demo service response：" + ans);

        Greeting greeting = helloWorldService.sayHello(new Person("Yi", "Hui>>" + UUID.randomUUID().toString()));
        System.out.println("helloWorld response: " + greeting);
        return ans + "|" + greeting;
    }
}
