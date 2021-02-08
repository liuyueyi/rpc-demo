package com.github.yihui.rpc.dubbo.api;

/**
 * @author wuzebang
 * @date 2021/2/8
 */
public interface HelloWorldServiceApi {

    /**
     * dubbo rpc service api
     * @param person
     * @return
     */
    Greeting sayHello(Person person);

}
