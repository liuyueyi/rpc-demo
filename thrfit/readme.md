thrift
---

thrift 使用示例工程

### 1. 环境准备

#### 1.1 thrift编译器准备

**MAC操作系统**

直接使用homebrew进行安装

```bash
brew install boost openssl libevent bison
brew install thrift
```

安装完毕之后，执行以下命令测试一下

```bash
which thrift

thrift --version
```

**WIN操作系统**

直接下载可执行的exe即可

- [https://thrift.apache.org/download](https://thrift.apache.org/download)

选中`Thrift compiler for Windows (thrift-0.13.0.exe)`下载即可;

```bash
# 打开 powershell

# 下载
wget http://www.apache.org/dyn/closer.cgi?path=/thrift/0.13.0/thrift-0.13.0.exe
# 重命名
mv thrift-0.13.0.exe thrift.exe
```

接下来，进行环境变量配置(windows常规的环境变量配置流程)

- 电脑右键 -> 属性 -> 高级系统设置 -> 环境变量 -> 系统环境变量 -> 新建 

```
变量名: thrift
变量值: 下载的thrift目录，如 D:\workspace\tools\thrift
```

- 接下来选中PATH变量，双击 -> 新建 -> 添加 `%thrift%` -> 确定

最后新开一个powshell终端，测试一下

```bash
thrift --version
```


#### 1.2 idea环境配置

> 暂时没有完成

首先安装插件: `thrift support`, 安装完毕之后重启

项目配置

- project settings -> facets -> add -> Thrift
- 在右边弹出的编辑框中，新增 `Target = Java, output path=输出Java文件路径`  
- 将thrift文件放在 `src/main/java` 代码目录下
- 右键 -> `Recompile xxx.thrift` (实测，这一步执行完毕之后并没有生成java文件)
 
### 2. thrift文件

thrift文件有自己的语法，这里不详细说明，一个helloword版内容如下

```thrift
namespace java com.github.yihui.rpc.thrfit.api

service HelloWorldService {
    Greeting sayHello(1: Person person);
}
struct Greeting {
1: required string message;
}

struct Person {
1: required string firstName;
2: required string lastName;
}
``` 

#### 2.1 直接编译

在前面thrift编译环境准备好之后，可以直接进行源文件的编译生成代码

```bash
## --out 表示输出的文件目录 （如果不指定时，输出文件会在当前目录的 gen-java 下面）
thrift --out ../java/ --gen java HelloWorldService.thrift
```

#### 2.2 maven插件编译

借助maven插件来实现thrfit文件的编译输出java代码，可能比直接运行命令更加合适，如`thrift-api`中，在pom文件中添加依赖

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.thrift.tools</groupId>
            <artifactId>maven-thrift-plugin</artifactId>
            <version>0.1.11</version>
            <configuration>
                <!--  请确保thrift编译器已配置，加入到系统环境  -->
                <thriftExecutable>thrift</thriftExecutable>
                <!--  thrift文件所在的目录 -->
                <thriftSourceRoot>src/main/thrift</thriftSourceRoot>
                <!--  输出的java文件目录，项目源码目录 -->
                <outputDirectory>${project.build.sourceDirectory}</outputDirectory>
                <!--  生产java文件 -->
                <generator>java</generator>
            </configuration>
            <executions>
                <execution>
                    <id>thrift-sources</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>compile</goal>
                    </goals>
                </execution>
                <execution>
                    <id>thrift-test-sources</id>
                    <phase>generate-test-sources</phase>
                    <goals>
                        <goal>testCompile</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

配置完毕之后， idea右侧边栏 -> `Maven` -> `thrift-api` -> `Plugins` -> `thrift` -> `thrift:compile`

代码生成完毕之后，会在对应的包路径下，看到生成的三个文件（`Person`, `Greeting`, `HelloWorldService`）

### 2. 基本使用

#### 2.1 Server

服务端通过实现`com.github.yihui.rpc.thrfit.api.HelloWorldService.Iface`接口来提供具体的服务

```java
public class HelloWorldServiceImpl implements HelloWorldService.Iface{
    @Override
    public Greeting sayHello(Person person) {
        System.out.println("receive person: " + person);
        Greeting greeting = new Greeting();
        greeting.setMessage("response for " + person);
        return greeting;
    }
}
```

接口实现之后，需要让我们的服务端跑起来提供服务，我们这里选择最简单的方式本机直连

核心代码如下，绑定端口号、指定协议，添加服务处理类

```java
HelloWorldServiceImpl helloWorldService = new HelloWorldServiceImpl();
HelloWorldService.Processor processor = new HelloWorldService.Processor<>(helloWorldService);
try {
    TThreadPoolServer.Args args = new TThreadPoolServer.Args(new TServerSocket(PORT)); // 绑定端口号
    args.processor(processor)
            .protocolFactory(new TBinaryProtocol.Factory()) // 定义传输协议
            .transportFactory(new TTransportFactory())
            .minWorkerThreads(1).maxWorkerThreads(5);
    server = new TThreadPoolServer(args);
    System.out.println("thrift server start by listen: " + PORT);
    server.serve();
} catch (Exception e) {
    e.printStackTrace();
}
```

#### 2.2 consumer

客户端如果需要访问远程服务，相比较grpc而言，要麻烦一点，当然第一步都一样，确定server地址，指定通讯协议

```java
private TTransport transport;
private TBinaryProtocol binaryProtocol;
private HelloWorldService.Client serverClient;

public HelloWorldProvider() {
    transport = new TSocket("localhost", 9000);
    binaryProtocol = new TBinaryProtocol(transport);
    serverClient = new HelloWorldService.Client(binaryProtocol);
}
```

接下来的服务通讯，主要借助`HelloWorldService.Client`来完成

```java
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
```

主要的麻烦点体现在上面的调用上，需要先执行`transport.open()`, 然后在发起请求，否则会抛`org.apache.thrift.transport.TTransportException: Cannot write to null outputstream`的异常

消费端的启动测试类就比较简单了，最基础的就行

```java
public class App {

    public static void main(String[] args) {
        HelloWorldProvider provider = new HelloWorldProvider();
        provider.sayHello("Yi", "Hui");
    }
}
```

#### 2.3 测试

测试步骤：
- 启动server
- 启动consumer
- 查看输出日志

**注意事项**

- 请注意server/consumer的transport保持一致，不然会抛出`org.apache.thrift.transport.TTransportException: java.net.SocketException: Connection reset`异常
