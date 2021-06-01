package com.th.dubbo.server.exception;

import cn.hutool.http.Method;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @author Lise
 * @date 2021年05月31日 20:13
 * @program: tanhua
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UnauthorizedException extends RuntimeException{


    private String url;
    private String body;
    private Method method;

}
