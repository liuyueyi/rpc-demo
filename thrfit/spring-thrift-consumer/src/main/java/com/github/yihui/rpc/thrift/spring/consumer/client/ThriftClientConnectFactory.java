package com.github.yihui.rpc.thrift.spring.consumer.client;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * @author wuzebang
 * @date 2021/2/9
 */
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
