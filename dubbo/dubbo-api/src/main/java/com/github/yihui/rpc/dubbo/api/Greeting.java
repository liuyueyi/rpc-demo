package com.github.yihui.rpc.dubbo.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author wuzebang
 * @date 2021/2/8
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Greeting implements Serializable {
    private String message;
}
