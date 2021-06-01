package com.th.dubbo.serverIsImpl.service;

import com.th.dubbo.serverIsImpl.enums.IdType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/*
 * @author Lise
 * @date 2021年05月27日 15:02
 * @program: tanhua
 * @description:
 */
@Service
public class idAuto {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    public Long created(IdType idType){
        String idkey ="TANHUA_ID_" + idType.toString();
        // 以增量的方式将double值存储在变量中。
        return this.redisTemplate.opsForValue().increment(idkey);
    }
}
