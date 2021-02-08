package com.github.yihui.grpc.basic.consumer;

import com.github.liuyueyi.grpc.api.Greeting;
import com.github.liuyueyi.grpc.api.HelloWorldServiceGrpc;
import com.github.liuyueyi.grpc.api.Person;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.netty.channel.nio.AbstractNioMessageChannel;

import java.util.concurrent.TimeUnit;

/**
 * @author yihui
 * @date 21/2/7
 */
public class HelloWordServiceConsumer {

    private ManagedChannel channel;
    private HelloWorldServiceGrpc.HelloWorldServiceBlockingStub helloWorldServiceBlockingStub;

    public HelloWordServiceConsumer() {
        channel = ManagedChannelBuilder.forAddress("127.0.0.1", 9000).usePlaintext().build();
        this.helloWorldServiceBlockingStub = HelloWorldServiceGrpc.newBlockingStub(channel);
    }

    public void destrory() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void sayHello(String firstName, String lastName) {
        Person req = Person.newBuilder().setFirstName(firstName).setLastName(lastName).build();
        Greeting res = helloWorldServiceBlockingStub.sayHello(req);
        System.out.println("res: " + res);
    }
}
