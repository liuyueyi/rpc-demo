package com.github.yihui.rpc.dubbo.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author wuzebang
 * @date 2021/2/8
 */
public class App {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"dubbo/*.xml", "spring/*.xml"});
        context.start();
        System.in.read(); // 按任意键退出
    }
}
