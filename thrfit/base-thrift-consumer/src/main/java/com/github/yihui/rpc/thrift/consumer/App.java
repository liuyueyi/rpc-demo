package com.github.yihui.rpc.thrift.consumer;

/**
 * @author wuzebang
 * @date 2021/2/8
 */
public class App {

    public static void main(String[] args) {
        HelloWorldProvider provider = new HelloWorldProvider();
        provider.sayHello("Yi", "Hui");
    }
}
