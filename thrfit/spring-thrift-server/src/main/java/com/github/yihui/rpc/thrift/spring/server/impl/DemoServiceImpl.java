package com.github.yihui.rpc.thrift.spring.server.impl;

import com.github.yihui.rpc.thrfit.api.DemoService;
import com.github.yihui.rpc.thrift.spring.server.ano.ThriftService;
import org.apache.thrift.TException;

/**
 * @author wuzebang
 * @date 2021/2/9
 */
@ThriftService(processor = DemoService.Processor.class)
public class DemoServiceImpl implements DemoService.Iface {

    @Override
    public int calculate(int a, int b) throws TException {
        return a + b;
    }
}
