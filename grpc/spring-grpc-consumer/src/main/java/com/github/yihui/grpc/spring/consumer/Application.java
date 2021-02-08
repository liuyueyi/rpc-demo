package com.github.yihui.grpc.spring.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author yihui
 * @date 21/2/7
 */
@SpringBootApplication
public class Application {

    public Application(HelloWordConsumer consumer) {
        consumer.sayHello("Yi", "Hui");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

}
