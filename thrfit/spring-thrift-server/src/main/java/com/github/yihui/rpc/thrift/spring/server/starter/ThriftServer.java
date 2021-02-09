package com.github.yihui.rpc.thrift.spring.server.starter;

import com.github.yihui.rpc.thrfit.api.HelloWorldService;
import com.github.yihui.rpc.thrift.spring.server.ano.ThriftService;
import com.github.yihui.rpc.thrift.spring.server.impl.HelloWorldServiceImpl;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wuzebang
 * @date 2021/2/9
 */
@Component
public class ThriftServer implements DisposableBean {
    @Value("${thrift.port}")
    private Integer port;

    @Value("${thrift.min-thread-pool}")
    private Integer minThreadPool;

    @Value("${thrift.max-thread-pool}")
    private Integer maxThreadPool;

    @Autowired
    private ApplicationContext applicationContext;

    private TServer server;

    @PostConstruct
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

            //添加业务
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

            // 服务名，com.github.yihui.rpc.thrfit.api.DemoService.Client
            String name = thriftServiceAno.processor().getName();
            name = name.substring(0, name.lastIndexOf("$Processor")) + "$Client";
            multiplexedProcessor.registerProcessor(name, (TProcessor) target.newInstance(value));
            System.out.println("register processor: " + name);
        }
        return multiplexedProcessor;
    }

    @Override
    public void destroy() throws Exception {
        if (server != null) {
            server.stop();
        }
    }
}
