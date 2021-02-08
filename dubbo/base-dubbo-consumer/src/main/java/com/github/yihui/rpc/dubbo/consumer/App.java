package com.github.yihui.rpc.dubbo.consumer;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author wuzebang
 * @date 2021/2/8
 */
public class App {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context =  new ClassPathXmlApplicationContext(new String[]{"dubbo/*.xml", "spring/*.xml"});
        context.start();
        HelloWorldProvider helloWorldProvider = context.getBean(HelloWorldProvider.class);
        helloWorldProvider.call();
        System.in.read(); // 按任意键退出
    }
}
