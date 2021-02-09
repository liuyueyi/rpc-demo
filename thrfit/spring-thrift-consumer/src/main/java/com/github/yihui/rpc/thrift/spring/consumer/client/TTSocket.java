package com.github.yihui.rpc.thrift.spring.consumer.client;

import lombok.Getter;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.net.Socket;

/**
 * @author wuzebang
 * @date 2021/2/9
 */
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

    /**
     * 打开通道
     *
     * @throws TTransportException
     */
    public void open() throws TTransportException {
        if (null != tTransport && !tTransport.isOpen()) {
            tTransport.open();
        }
    }

    /**
     * 关闭通道
     */
    public void close() {
        if (null != tTransport && tTransport.isOpen()) {
            tTransport.close();
        }
    }

    /**
     * 判断通道是否是正常打开状态
     *
     * @return
     */
    public boolean isOpen() {
        Socket socket = tSocket.getSocket();
        return socket.isConnected() && !socket.isClosed();
    }
}
