package com.github.yihui.rpc.thrift.server;

import com.github.yihui.rpc.thrfit.api.HelloWorldService;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportFactory;

/**
 * @author wuzebang
 * @date 2021/2/8
 */
public class App {
    private static final int PORT = 9000;
    TServer server;

    public void start() {
        HelloWorldServiceImpl helloWorldService = new HelloWorldServiceImpl();
        HelloWorldService.Processor processor = new HelloWorldService.Processor<>(helloWorldService);
        try {
            TServerTransport transport = new TServerSocket(PORT);
            TThreadPoolServer.Args args = new TThreadPoolServer.Args(transport);
            args.processor(processor)
                    .protocolFactory(new TBinaryProtocol.Factory())
                    .transportFactory(new TTransportFactory())
                    .minWorkerThreads(1).maxWorkerThreads(5);
            server = new TThreadPoolServer(args);
            System.out.println("thrift server start by listen: " + PORT);
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public synchronized void start() {
                System.out.println("shutting down thrift server");
                App.this.stop();
                System.out.println("already shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.stop();
        }
    }

    public static void main(String[] args) {
        App app = new App();
        app.start();
    }

}
