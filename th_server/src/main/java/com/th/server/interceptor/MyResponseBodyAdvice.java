package com.th.server.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.th.common.utils.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.concurrent.TimeUnit;

/*
 * @author Lise
 * @date 2021年05月26日 0:02
 * @program: tanhua
 * @description:
 */
public class MyResponseBodyAdvice implements ResponseBodyAdvice {


    @Value("${tanhua.cache.enable}")
    private Boolean enable;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        // 开关处于开启状态  是get请求  包含了@Cache注解
        MethodParameter returnType;
        return enable && methodParameter.hasMethodAnnotation(GetMapping.class)
                && methodParameter.hasMethodAnnotation(Cache.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType, Class aClass,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (null == body) {
            return null;
        }

        try {

            String redisValue = null;
            if (body instanceof String) {
                redisValue = (String) body;
            } else {
                redisValue = MAPPER.writeValueAsString(body);
            }

            String redisKey = RedisCacheInterceptor.createRedisKey(((ServletServerHttpRequest) request).getServletRequest());

            Cache cache = methodParameter.getMethodAnnotation(Cache.class);

            //缓存的时间单位是秒
            this.redisTemplate.opsForValue().set(redisKey, redisValue, Long.valueOf(cache.time()), TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return body;
    }
}
