package com.th.dubbo.server.service;

/*
 * @author Lise
 * @date 2021年05月31日 20:11
 * @program: tanhua
 */

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import com.th.dubbo.server.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RequestService {

    @Autowired
    private TokenService tokenService;


    @Retryable(value = UnauthorizedException.class, maxAttempts = 5, backoff = @Backoff(delay = 2000L, multiplier = 2))
    public HttpResponse execute(String url, String body, Method method) {
        String token = this.tokenService.getToken();

        HttpRequest httpRequest;

        switch (method) {
            case POST: {
                httpRequest = HttpRequest.post(url);
                break;
            }
            case DELETE: {
                httpRequest = HttpRequest.delete(url);
                break;
            }
            case PUT: {
                httpRequest = HttpRequest.put(url);
                break;
            }
            case GET: {
                httpRequest = HttpRequest.get(url);
                break;
            }
            default: {
                return null;
            }
        }

        HttpResponse response = httpRequest
                .header("Content-Type", "application/json") //设置请求内容类型
                .header("Authorization", "Bearer " + token)  //设置token
                .body(body) // 设置请求数据
                .timeout(20000) // 超时时间
                .execute(); // 执行请求

        if (response.getStatus() ==401){
            // token 失效 , 重新获取
            //
            this.tokenService.refreshToken();

            //抛出异常，需要进行重试
            throw new UnauthorizedException(url, body, method);
        }

        return response;
    }


    @Recover //全部重试失败后执行
    public HttpResponse recover(UnauthorizedException e) {
        log.error("获取token失败！url = " + e.getUrl() + ", body = " + e.getBody() + ", method = " + e.getMethod().toString());
        //如果重试5次后，依然不能获取到token，说明网络或账号出现了问题，只能返回null了，后续的请求将无法再执行
        return null;
    }
}
