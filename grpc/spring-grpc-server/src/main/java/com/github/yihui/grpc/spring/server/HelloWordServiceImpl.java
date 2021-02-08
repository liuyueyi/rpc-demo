package com.github.yihui.grpc.spring.server;

import com.github.liuyueyi.grpc.api.Greeting;
import com.github.liuyueyi.grpc.api.HelloWorldServiceGrpc;
import com.github.liuyueyi.grpc.api.Person;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * @author yihui
 * @date 21/2/7
 */
@GrpcService
public class HelloWordServiceImpl extends HelloWorldServiceGrpc.HelloWorldServiceImplBase {

    @Override
    public void sayHello(Person request, StreamObserver<Greeting> responseObserver) {
        System.out.println("receiver req: " + request);
        responseObserver.onNext(Greeting.newBuilder().setMessage("hello " + request).build());
        responseObserver.onCompleted();
    }
}
