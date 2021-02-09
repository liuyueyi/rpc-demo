package com.github.yihui.rpc.thrift.spring.consumer.util;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author wuzebang
 * @date 2021/2/9
 */
public class ProxyUtil {
    public static <T> T newProxyInstance(Class<?> targetClass, InvocationHandler invocationHandler,
                                         ProxyUtil.CallbackFilter filter, Class[] argumentTypes, Object[] arguments) {
        if (targetClass == null) {
            return null;
        } else {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(targetClass);
            enhancer.setCallback(new ProxyUtil.SimpleMethodInterceptor(invocationHandler, filter));
            return (T) enhancer.create(argumentTypes, arguments);
        }
    }

    public interface CallbackFilter {
        boolean accept(Method var1);
    }

    private static class SimpleMethodInterceptor implements MethodInterceptor, Serializable {
        private transient InvocationHandler invocationHandler;
        private transient ProxyUtil.CallbackFilter filter;

        public SimpleMethodInterceptor(InvocationHandler invocationHandler, ProxyUtil.CallbackFilter filter) {
            this.invocationHandler = invocationHandler;
            this.filter = filter;
        }

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            return this.filter.accept(method) ? this.invocationHandler.invoke(o, method, objects) : methodProxy.invokeSuper(o, objects);
        }
    }
}
