package com.github.yihui.grpc.basic.consumer;

/**
 * @author yihui
 * @date 21/2/7
 */
public class App {

    public static void main(String[] args) throws InterruptedException {
        HelloWordServiceConsumer consumer = new HelloWordServiceConsumer();
        consumer.sayHello("Yi", "Hui");
        consumer.destrory();
    }

}
