package com.github.yihui.grpc.spring.consumer;

import com.github.liuyueyi.grpc.api.Greeting;
import com.github.liuyueyi.grpc.api.HelloWorldServiceGrpc;
import com.github.liuyueyi.grpc.api.Person;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

/**
 * @author yihui
 * @date 21/2/7
 */
@Service
public class HelloWordConsumer {
    @GrpcClient("GLOBAL")
    HelloWorldServiceGrpc.HelloWorldServiceBlockingStub helloWorldServiceBlockingStub;

    public void sayHello(String first, String last) {
        Greeting re = helloWorldServiceBlockingStub.sayHello(Person.newBuilder().setFirstName(first).setLastName(last).build());
        System.out.println(re);
    }
}
