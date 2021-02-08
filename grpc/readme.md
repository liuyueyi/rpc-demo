grpc
---

基于官方原生的grpc的一个java版小demo

参考链接：
- [https://github.com/grpc/grpc-java](https://github.com/grpc/grpc-java)
- [IDEA java开发 grpc框架的服务端和客户端--helloworld实例](https://blog.csdn.net/qq_29319189/article/details/93539198)

- [gRPC Java Example](https://codenotfound.com/grpc-java-example.html)


### 1. 基础版入门

#### 1.1 依赖

grpc基本依赖包

```xml
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-netty-shaded</artifactId>
    <version>1.35.0</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-protobuf</artifactId>
    <version>1.35.0</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-stub</artifactId>
    <version>1.35.0</version>
</dependency>
```

借助maven的插件，来实现解析proto文件，生成对应的代码

```xml
<extensions>
    <extension>
        <groupId>kr.motd.maven</groupId>
        <artifactId>os-maven-plugin</artifactId>
        <version>1.6.2</version>
    </extension>
</extensions>

<plugins>
    <!-- protobuf-maven-plugin -->
    <plugin>
        <groupId>org.xolstice.maven.plugins</groupId>
        <artifactId>protobuf-maven-plugin</artifactId>
        <version>0.5.1</version>
        <configuration>
            <protocArtifact>com.google.protobuf:protoc:3.12.0:exe:${os.detected.classifier}</protocArtifact>
            <pluginId>grpc-java</pluginId>
            <pluginArtifact>io.grpc:protoc-gen-grpc-java:1.35.0:exe:${os.detected.classifier}</pluginArtifact>
            <!-- proto文件路径 -->
            <protoSourceRoot>src/main/proto</protoSourceRoot>
            <!--  确保生成的Java文件在项目目录中  -->
            <outputDirectory>${project.build.sourceDirectory}</outputDirectory>
            <clearOutputDirectory>false</clearOutputDirectory>
        </configuration>
        <executions>
            <execution>
                <goals>
                    <goal>compile</goal>
                    <goal>compile-custom</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
</plugins>
```

#### 1.2 proto定义+编译

在api项目环境下，新建目录 `src/main/proto`， 这个对应的是上面`plugin`中的 `protoSourceRoot`

一个基础的定义如下

```proto
syntax = "proto3";

option java_multiple_files = true;
package com.mi.zbwu.grpc.api;

message Person {
string first_name = 1;
string last_name = 2;
}

message Greeting {
string message = 1;
}

service HelloWorldService {
rpc sayHello (Person) returns (Greeting);
}
```

这里介绍基于maven的插件来实现解析的方式

IDEA -> 右侧边栏点击maven -> 找到项目，plugins -> 选中protobuf -> 点击`protobuf:compile` -> 点击`protobuf:compile-custom`

**注意：依次执行compile, compile-custom**

完成之后就可以在api下看到生成的代码了

#### 1.3 服务端

服务端，首先添加api的依赖，没什么可说的

服务接口实现类`HelloWorldServiceImpl`，需要继承自API中的`HelloWorldServiceGrpc.HelloWorldServiceImplBase`

```java
@Slf4j
@Service
public class HelloWorldServiceImpl extends HelloWorldServiceGrpc.HelloWorldServiceImplBase {

    @Override
    public void sayHello(Person request, StreamObserver<Greeting> responseObserver) {
        System.out.println("Receiver: " + request);
        // 返回结果
        responseObserver.onNext(Greeting.newBuilder().setMessage("response by server for: " + request.toString()).build());
        responseObserver.onCompleted();
    }
}
```

**注意：**
- 虽然我们定义的接口形如: `Greeting sayHello(Person request)`，当时实际实现却不如此
- grpc通过`StreamObserver`实现观察者模式，感觉上和react有点相似

上面虽然是实现了服务接口，当时如果希望提供rpc服务的话，还需要我们创建对应的server（和thrfit有些类似）

```java
@Slf4j
public class GRpcServer {

    private Server server;

    public void start(HelloWorldServiceImpl helloWorldService) throws IOException {
        /* The port on which the server should run */
        int port = 9000;
        server = ServerBuilder.forPort(port)
                .addService(helloWorldService)
                .build()
                .start();
        log.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                GRpcServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
```

最后基于SpringBoot包一个启动容器的壳，让应用跑起来

```java
@Slf4j
@SpringBootApplication
public class Application {
    @Autowired
    private HelloWorldServiceImpl helloWorldService;

    @PostConstruct
    public void toInit() {
        try {
            GRpcServer gRpcServer = new GRpcServer();
            gRpcServer.start(helloWorldService);
            gRpcServer.blockUntilShutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
```

#### 1.4 客户端

接下来作为消费者，希望访问目标对象，相对而言会简单一点

```java
@Slf4j
@Service
public class HelloWorldConsumer implements DisposableBean {

    private ManagedChannel channel;
    private HelloWorldServiceGrpc.HelloWorldServiceBlockingStub blockingStub;

    @PostConstruct
    public void init() {
        this.channel = ManagedChannelBuilder.forAddress("127.0.0.1", 9000)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build();
        this.blockingStub = HelloWorldServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public void destroy() throws Exception {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * 下面这个是用于测试通信的方法
     */
    public void hello(String fName, String lName) {
        log.info("Will try to greet " + fName + " ...");
        Person request = Person.newBuilder().setFirstName(fName).setLastName(lName).build();
        Greeting response;
        try {
            response = blockingStub.sayHello(request);
        } catch (StatusRuntimeException e) {
            log.warn("RPC failed: {}", e.getStatus());
            return;
        }
        log.info("Greeting: {}", response.getMessage());
    }
}
```

上面引入一个Channel和Stub，可以简单的将Channe理解为指定与Server通信的socket，Stub理解为HelloWorldService的代理

同样基于SpringBoot包装一个可以运行的容器，进行测试访问

```java
@SpringBootApplication
public class Application {

    public Application(HelloWorldConsumer helloWorldConsumer) {
        helloWorldConsumer.hello("yi", "hui");
        System.out.println("----over---");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
```

#### 1.5 测试及小结

- 先启动grpc-server 
- 其次启动grpc-consumer

然后查看两个输出日志（当然也可以debug，查看具体的调用）

整体感受下来，grpc的原生使用姿势和thrift差不多；没有dubbo/Spring Cloud用起来顺手

### 2. Spring集成版

基于 [https://github.com/yidongnan/grpc-spring-boot-starter](https://github.com/yidongnan/grpc-spring-boot-starter) 来实现Spring生态下更友好的使用方式

#### 0. proto + api

proto文件定义与编译，和前面都一样，这里不赘述

#### 1. server

服务端添加依赖，最核心的是 `grpc-server-spring-boot-starter` 

```xml
<dependencies>
    <dependency>
        <groupId>com.mi.zbwu</groupId>
        <artifactId>grpc-api</artifactId>
        <version>1.0-SNAPSHOT</version>
        <exclusions>
            <exclusion>
                <groupId>io.grpc</groupId>
                <artifactId>grpc-stub</artifactId>
            </exclusion>
            <exclusion>
                <groupId>io.grpc</groupId>
                <artifactId>grpc-protobuf</artifactId>
            </exclusion>
            <exclusion>
                <groupId>io.grpc</groupId>
                <artifactId>grpc-netty-shaded</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>net.devh</groupId>
        <artifactId>grpc-server-spring-boot-starter</artifactId>
        <version>2.10.1.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

接口实现，和前面没什么区别，只是在类上添加注解`@GrpcService`

```java
@GrpcService
public class HelloWorldServiceImpl extends HelloWorldServiceGrpc.HelloWorldServiceImplBase {

    @Override
    public void sayHello(Person request, StreamObserver<Greeting> responseObserver) {
        System.out.println("Receiver: " + request);
        // 返回结果
        responseObserver.onNext(Greeting.newBuilder().setMessage("response by server for: " + request.toString()).build());
        responseObserver.onCompleted();
    }
}
```

在SpringBoot项目中，就不需要像之前一样设置GrpcServer，最常见的SpringBoot启动方式即可

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
```

通过配置文件指定grpc提供服务的端口号

```yml
grpc:
  server:
    port: 9000
```

#### 2. consumer

消费端，作为调用方，主要需要引入的依赖是`grpc-spring-boot-starter`

```xml
<dependencies>
    <dependency>
        <groupId>com.mi.zbwu</groupId>
        <artifactId>grpc-api</artifactId>
        <version>1.0-SNAPSHOT</version>
        <exclusions>
            <exclusion>
                <groupId>io.grpc</groupId>
                <artifactId>grpc-stub</artifactId>
            </exclusion>
            <exclusion>
                <groupId>io.grpc</groupId>
                <artifactId>grpc-protobuf</artifactId>
            </exclusion>
            <exclusion>
                <groupId>io.grpc</groupId>
                <artifactId>grpc-netty-shaded</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>net.devh</groupId>
        <artifactId>grpc-spring-boot-starter</artifactId>
        <version>2.10.1.RELEASE</version>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

相比之前的使用方，这里简洁太多了，直接注入使用即可

```java
@Service
public class HelloWorldConsumer {
    @GrpcClient("GLOBAL")
    private HelloWorldServiceGrpc.HelloWorldServiceBlockingStub helloWorldServiceBlockingStub;

    public void sayHello() {
        Greeting greeting = helloWorldServiceBlockingStub.sayHello(Person.newBuilder().setFirstName("yi").setLastName("hui").build());
        System.out.println(greeting);
    }
}
```

**注意**

- 注解`GrpcClient`，通过配置文件来获取server端的地址，这里演示的是本地直连

对应的配置文件

```yaml
grpc:
  client:
    GLOBAL:
      address: 'static://127.0.0.1:9000'
      enableKeepAlive: true
      keepAliveWithoutCalls: true
      negotiationType: plaintext
```

启动测试类

```java
@SpringBootApplication
public class Application {

    public Application(HelloWorldConsumer helloWorldConsumer) {
        helloWorldConsumer.sayHello();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

}
```

