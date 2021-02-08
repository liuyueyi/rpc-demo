package com.github.yihui.rpc.thrift.consumer;

import com.github.yihui.rpc.thrfit.api.Greeting;
import com.github.yihui.rpc.thrfit.api.HelloWorldService;
import com.github.yihui.rpc.thrfit.api.Person;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 * @author wuzebang
 * @date 2021/2/8
 */
public class HelloWorldProvider {
    private TTransport transport;
    private TBinaryProtocol binaryProtocol;
    private HelloWorldService.Client serverClient;

    public HelloWorldProvider() {
        transport = new TSocket("localhost", 9000);
        binaryProtocol = new TBinaryProtocol(transport);
        serverClient = new HelloWorldService.Client(binaryProtocol);
    }

    public void sayHello(String first, String last) {
        try {
            if (transport != null && !transport.isOpen()) {
                transport.open();
            }

            Person person = new Person(first, last);
            Greeting greeting = serverClient.sayHello(person);
            System.out.println("Receive : " + greeting);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (transport != null && !transport.isOpen()) {
                transport.close();
            }
        }
    }

}
