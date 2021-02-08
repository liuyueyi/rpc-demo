package com.github.yihui.grpc.basic.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

/**
 * @author yihui
 * @date 21/2/7
 */
public class App {
    private Server server;
    private final int port = 9000;

    public void start(HelloWordServiceImpl helloWordService) throws Exception {
        server = ServerBuilder.forPort(port).addService(helloWordService)
                .build().start();

        System.out.println("server started on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public synchronized void start() {
                System.out.println("shutting down grpc erver");
                App.this.stop();
                System.out.println("already shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws Exception {
        App app = new App();
        app.start(new HelloWordServiceImpl());
        app.blockUntilShutdown();
    }
}
