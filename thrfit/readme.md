thrift
---

thrift 使用示例工程

## 1. 环境准备

### 1.1 thrift编译器准备

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

## 2. 基本使用

### 2.1 Server

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

### 2.2 consumer

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

### 2.3 测试

测试步骤：
- 启动server
- 启动consumer
- 查看输出日志

**注意事项**

- 请注意server/consumer的transport保持一致，不然会抛出`org.apache.thrift.transport.TTransportException: java.net.SocketException: Connection reset`异常


## 3. Spring继承

直接用上面的方式使用thrift，最直观的感觉就是用起来太不顺手了，接下来介绍一下spring环境下，如何更好的使用thrift

### 3.1 多服务接口定义

> 项目 [thrift-api](thrift-api)

实际的项目中，不太可能只有一个服务，接下来的Spring继承项目中，新增一个thrift接口

除了前面的`HelloWorldService.thrift`文件之外，新增一个简单的 `DemoService.thrift`

```thrift
namespace java com.github.yihui.rpc.thrfit.api
service DemoService {
     i32 calculate(1: i32 a, 2: i32 b);
}
```

### 3.2 服务端

> 项目 [spring-thrift-server](spring-thrift-server)

#### 3.2.1 服务实现

首先定义两个Service，实现上面的接口

```java
@ThriftService(processor = DemoService.Processor.class)
public class DemoServiceImpl implements DemoService.Iface {

    @Override
    public int calculate(int a, int b) throws TException {
        return a + b;
    }
}

@ThriftService(processor = HelloWorldService.Processor.class)
public class HelloWorldServiceImpl implements HelloWorldService.Iface {

    @Override
    public Greeting sayHello(Person person) throws TException {
        System.out.println("thrift server receive: " + person);
        return new Greeting("receive: " + person);
    }
}
```

#### 3.2.2 服务注册

接口实现比较容易，接下来需要注意thrift服务的开启；因为使用多服务注册，这个时候需要`TMultiplexedProcessor`来替代前面写死的`HelloWorldService.Processor processor = new HelloWorldService.Processor<>(helloWorldService);`

因此我们的start逻辑将如下

```java
public void starter() {
    try {
        TNonblockingServerSocket socket = new TNonblockingServerSocket(port);

        THsHaServer.Args args = new THsHaServer.Args(socket)
                .minWorkerThreads(minThreadPool)
                .maxWorkerThreads(maxThreadPool);

        //Thrift传输协议
        //1. TBinaryProtocol      二进制传输协议
        //2. TCompactProtocol     压缩协议 他是基于TBinaryProtocol二进制协议在进一步的压缩，使得体积更小
        //3. TJSONProtocol        Json格式传输协议
        //4. TSimpleJSONProtocol  简单JSON只写协议，生成的文件很容易通过脚本语言解析，实际开发中很少使用
        //5. TDebugProtocol       简单易懂的可读协议，调试的时候用于方便追踪传输过程中的数据
        args.protocolFactory(new TCompactProtocol.Factory());

        //Thrift数据传输方式
        //1. TSocker            阻塞式Scoker 相当于Java中的ServerSocket
        //2. TFrameTransport    以frame为单位进行数据传输，非阻塞式服务中使用
        //3. TFileTransport     以文件的形式进行传输
        //4. TMemoryTransport   将内存用于IO,Java实现的时候内部实际上是使用了简单的ByteArrayOutputStream
        //5. TZlibTransport     使用zlib进行压缩，与其他传世方式联合使用；java当前无实现所以无法使用
        args.transportFactory(new TFramedTransport.Factory());

        // 添加业务
        TProcessor processor = this.loadProcessor();
        args.processor(processor);

        //thrift支持的服务模型
        //1.TSimpleServer  简单的单线程服务模型，用于测试
        //2.TThreadPoolServer 多线程服务模型，使用的标准的阻塞式IO;运用了线程池，当线程池不够时会创建新的线程,当线程池出现大量空闲线程，线程池会对线程进行回收
        //3.TNonBlockingServer 多线程服务模型，使用非阻塞式IO（需要使用TFramedTransport数据传输方式）
        //4.THsHaServer YHsHa引入了线程池去处理（需要使用TFramedTransport数据传输方式），其模型把读写任务放到线程池去处理;Half-sync/Half-async（半同步半异步）的处理模式;Half-sync是在处理IO时间上（sccept/read/writr io）,Half-async用于handler对RPC的同步处理
        server = new THsHaServer(args);

        System.out.println("Thrift Server 端口：" + port + "， 启动成功");
        //启动server
        server.serve();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

上面的实现与前面的basic-demo没有什么区别，关键在于`TMultiplexedProcessor`的创建，它的基本使用姿势如

```java
multiplexedProcessor.registerProcessor(name, TProcessor);
```

所以最主要的关键点就是创建`TProcessor`实例，一个最简单、直观的实现姿势就是直接反射创建，新增一个注解`@ThriftService`，用来修饰Thrift服务

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface ThriftService {
    Class<? extends TProcessor> processor();
}
```

最终`TMultiplexedProcessor`的注册姿势实现如下

```java
private TProcessor loadProcessor() throws IllegalAccessException, InvocationTargetException, InstantiationException {
    TMultiplexedProcessor multiplexedProcessor = new TMultiplexedProcessor();

    // 扫描所有的Thrift服务并注册
    Map<String, Object> obj = applicationContext.getBeansWithAnnotation(ThriftService.class);
    for (Object value : obj.values()) {
        Map<Class, Object> map = new HashMap<>();
        for (Class face : value.getClass().getInterfaces()) {
            map.put(face, null);
        }

        ThriftService thriftServiceAno = value.getClass().getAnnotation(ThriftService.class);
        // 因为没有无参构造方法，所以需要加上下面这个参数匹配过滤
        Constructor[] constructors = thriftServiceAno.processor().getConstructors();
        Constructor target = null;
        for (Constructor constructor : constructors) {
            Class[] clzList = constructor.getParameterTypes();
            for (Class pClz : clzList) {
                if (map.containsKey(pClz)) {
                    target = constructor;
                    break;
                }
            }
            if (target != null) {
                break;
            }
        }

        // 服务名，如： com.github.yihui.rpc.thrfit.api.DemoService$Client
        String name = thriftServiceAno.processor().getName();
        name = name.substring(0, name.lastIndexOf("$Processor")) + "$Client";
        multiplexedProcessor.registerProcessor(name, (TProcessor) target.newInstance(value));
        System.out.println("register processor: " + name);
    }
    return multiplexedProcessor;
}
```

**注意事项**

- `multiplexedProcessor.registerProcessor(name, TProcessor)`: 
    - 第一个参数name，因为客户端访问时需要和服务端保持一致
    - 里默认采用的是thrift自动生成java类中的Client的全名
    
#### 3.2.3 服务配置

在看项目源码时，会看到几个参数，比较核心的有下面三个

```yaml
thrift:
  # thrift 端口号
  port: 9000
  # 服务端最小线程池数
  min-thread-pool: 4
  # 服务端最小线程池数
  max-thread-pool: 8
```

### 3.3 consumer

> 项目 [spring-thrift-consumer](spring-thrift-consumer)

#### 3.3.0 待解决点

通过前面的consumer的使用姿势，会发现有几个地方用起来不太顺手

- 直接注入 `xxx.Client` 实现服务接口访问
- 直接调用，而不是每次访问之前，都先调用一下`TTransport#open()`
- 连接池复用

#### 3.3.1 连接池

使用连接池支持复用，封装一个基础的`TTSocket`，放在连接池中

```java
public class TTSocket {
    /**
     * thrift socket对象
     */
    private TSocket tSocket;

    /**
     * 传输对象
     */
    private TTransport tTransport;

    /**
     * 协议对象
     */
    @Getter
    private TProtocol tProtocol;

    @Getter
    private boolean multi;

    public TProtocol getTProtocol(Class client) {
        if (multi) {
            return new TMultiplexedProtocol(tProtocol, client.getName());
        } else {
            return tProtocol;
        }
    }

    /**
     * 构造方法初始化各个连接对象
     *
     * @param host server的地址
     * @param port server的端口
     */
    public TTSocket(String host, Integer port, boolean multi) {
        tSocket = new TSocket(host, port);
        tTransport = new TFramedTransport(tSocket, 600);
        //协议对象 这里使用协议对象需要和服务器的一致
        tProtocol = new TCompactProtocol(tTransport);
        this.multi = multi;
    }
    
// 省略其他方法
}
```

连接池使用common-pool来支持，基本知识点就是连接池的使用姿势

```java
public class ThriftClientConnectFactory {
    private GenericObjectPool<TTSocket> pool;

    public ThriftClientConnectFactory(ThriftConfig thriftConfig) {
        ConnectionFactory factory = new ConnectionFactory(thriftConfig.getHost(), thriftConfig.getPort(), thriftConfig.getMulti());
        //实例化池对象
        GenericObjectPool.Config config = new GenericObjectPool.Config();
        config.minIdle = thriftConfig.getMinThreadPool();
        config.maxActive = thriftConfig.getMaxThreadPool();
        this.pool = new GenericObjectPool<>(factory, config);
        //设置获取对象前校验对象是否可以
        this.pool.setTestOnBorrow(true);
    }

    /**
     * 在池中获取一个空闲的对象
     * 如果没有空闲且池子没满，就会调用makeObject创建一个新的对象
     * 如果满了，就会阻塞等待，直到有空闲对象或者超时
     *
     * @return
     * @throws Exception
     */
    public TTSocket getConnect() throws Exception {
        return pool.borrowObject();
    }

    /**
     * 将对象从池中移除
     *
     * @param ttSocket
     */
    public void invalidateObject(TTSocket ttSocket) {
        try {
            pool.invalidateObject(ttSocket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将一个用完的对象返还给对象池
     *
     * @param ttSocket
     */
    public void returnConnection(TTSocket ttSocket) {
        try {
            pool.returnObject(ttSocket);
        } catch (Exception e) {
            if (ttSocket != null) {
                try {
                    ttSocket.close();
                } catch (Exception ex) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 池里面保存的对象工厂
     */
    static class ConnectionFactory extends BasePoolableObjectFactory<TTSocket> {
        /**
         * 远端地址
         */
        private String host;

        /**
         * 端口号
         */
        private Integer port;

        /**
         * true 表示服务端一个端口对应多个服务
         */
        private Boolean multi;

        /**
         * 构造方法初始化地址及端口
         *
         * @param ip
         * @param port
         */
        public ConnectionFactory(String ip, int port, boolean multi) {
            this.host = ip;
            this.port = port;
            this.multi = multi;
        }

        /**
         * 创建一个对象
         *
         * @return
         * @throws Exception
         */
        @Override
        public TTSocket makeObject() throws Exception {
            // 实例化一个自定义的一个thrift 对象
            TTSocket ttSocket = new TTSocket(host, port, multi);
            // 打开通道
            ttSocket.open();
            return ttSocket;
        }

        /**
         * 销毁对象
         *
         * @param obj
         */
        @Override
        public void destroyObject(TTSocket obj) {
            try {
                if (obj != null) {
                    //尝试关闭连接
                    obj.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 校验对象是否可用
         * 通过 pool.setTestOnBorrow(boolean testOnBorrow) 设置
         * 设置为true这会在调用pool.borrowObject()获取对象之前调用这个方法用于校验对象是否可用
         *
         * @param obj 待校验的对象
         * @return
         */
        @Override
        public boolean validateObject(TTSocket obj) {
            if (obj != null) {
                return obj.isOpen();
            }
            return false;
        }
    }
}
```

#### 3.3.2 配置

消费端，需要的配置thrift服务端信息，客户端连接池信息，实例demo中的配置文件如下

```yaml
thrift:
  host: localhost
  port: 9000
  min-thread-pool: 4
  max-thread-pool: 8
  multi: true
```

**请注意**
- multi: 当服务端是一个端口提供多个服务时，设置为true，因为需要采用`TMultiplexedProtocol`协议来访问；如果时false，则和服务端保持一致

这里直接借助Spring的配置绑定来实现配置解析，具体查看项目源码

#### 3.3.3 client代理

接下来重点来了，我们系统通过自定义的注解`@ThriftClient`，来主动注入Client对应的代理类，来封装每次访问之前的前置操作

借助`BeanPostProcessor`来实现bean创建完毕之后，注册代理服务

- 扫描有`@ThriftClient`注解的成员，手动注入代理对象

```java
@Component
public class ThriftClientPostProcessor implements BeanPostProcessor {
    private final ApplicationContext applicationContext;

    public ThriftClientPostProcessor(final ApplicationContext applicationContext) {
        this.applicationContext = requireNonNull(applicationContext, "applicationContext");
    }

    /**
     * 扫描bean中有@ThriftClient注解的成员变量，并手动注入代理对象
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        Class<?> clz = bean.getClass();
        do {
            for (final Field clientField : clz.getDeclaredFields()) {
                final ThriftClient clientAnno = AnnotationUtils.findAnnotation(clientField, ThriftClient.class);
                if (clientAnno != null) {
                    ReflectionUtils.makeAccessible(clientField);
                    ReflectionUtils.setField(clientField, bean, processInjectionPoint(clientField.getType()));
                }
            }

            clz = clz.getSuperclass();
        } while (clz != null);
        return bean;
    }
    
}
```

- 创建代理类

```java
protected <T> T processInjectionPoint(final Class<T> thriftClient) {
    try {
        return ProxyUtil.newProxyInstance(thriftClient, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                ThriftClientConnectFactory factory = applicationContext.getBean(ThriftClientConnectFactory.class);

                TTSocket socket = null;
                try {
                    socket = factory.getConnect();
                    TProtocol protocol = socket.getTProtocol(thriftClient);
                    // 创建 xxx.Client 实例
                    Object client = thriftClient.getConstructor(TProtocol.class).newInstance(protocol);
                    return method.invoke(client, args);
                } catch (Exception e) {
                    if (socket != null) {
                        factory.invalidateObject(socket);
                    }
                    throw e;
                } finally {
                    if (socket != null) {
                        factory.returnConnection(socket);
                    }
                }
            }
        }, new ProxyUtil.CallbackFilter() {
            @Override
            public boolean accept(Method method) {
                return true;
            }
        }, new Class[]{
                org.apache.thrift.protocol.TProtocol.class
        }, new Object[]{
                getDefaultProtocol(thriftClient),
        });
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}

private TProtocol getDefaultProtocol(final Class serviceClient) throws Exception {
    ThriftClientConnectFactory factory = applicationContext.getBean(ThriftClientConnectFactory.class);
    return factory.getConnect().getTProtocol(serviceClient);
}
```

#### 3.3.4 测试实例

接下来写一个测试类，看一下最终效果如何

```java
@RestController
public class TestConsumer {
    @ThriftClient
    private DemoService.Client demoService;
    @ThriftClient
    private HelloWorldService.Client helloWorldService;

    @GetMapping(path = "test")
    public String test() throws TException {
        int ans = demoService.calculate(10, (int) (Math.random() * 100));
        System.out.println("demo service response：" + ans);

        Greeting greeting = helloWorldService.sayHello(new Person("Yi", "Hui>>" + UUID.randomUUID().toString()));
        System.out.println("helloWorld response: " + greeting);
        return ans + "|" + greeting;
    }
}
```

上面的使用case，基本上和我们常规的Spring使用方法保持一致了，加一个注解，就像本地接口访问一样，这样的使用姿势才是我们熟悉的味道