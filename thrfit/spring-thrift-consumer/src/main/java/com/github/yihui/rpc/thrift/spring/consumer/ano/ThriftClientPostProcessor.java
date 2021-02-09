package com.github.yihui.rpc.thrift.spring.consumer.ano;

import com.github.yihui.rpc.thrift.spring.consumer.client.TTSocket;
import com.github.yihui.rpc.thrift.spring.consumer.client.ThriftClientConnectFactory;
import com.github.yihui.rpc.thrift.spring.consumer.util.ProxyUtil;
import org.apache.thrift.protocol.TProtocol;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;

/**
 * @author wuzebang
 * @date 2021/2/9
 */
@Component
public class ThriftClientPostProcessor implements BeanPostProcessor {
    private final ApplicationContext applicationContext;

    public ThriftClientPostProcessor(final ApplicationContext applicationContext) {
        this.applicationContext = requireNonNull(applicationContext, "applicationContext");
    }

    /**
     * 扫描bean中有@ThriftClient注解的成员变量，并手动注入
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
}
