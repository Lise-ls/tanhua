package com.th.sso.service;

/*
 * @author Lise
 * @date 2021年05月21日 21:21
 * @program: tanhua
 * @description:
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.th.common.mapper.UserMapper;
import com.th.common.pojo.User;
import com.th.common.utils.AppJwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;



import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserService {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Resource
    private UserMapper userMapper;

    @Value("${jwt.secret}")
    private String secret;

    public String login(String phone, String code) {

        String redisKey = "CHECK_CODE_" + phone;
        boolean isNew = false;

        // 根据redis客户端 传入手机号 获取验证码
        String redisCode = redisTemplate.opsForValue().get(redisKey);

        // 打印
        System.out.println("UserService...redisCode = " + redisCode);
        System.out.println("UserService...code = " + code);

        // 进行if判断 不一致则验证码错误
        if (!StringUtils.equals(code,redisCode)){
            return null;
        }

        // 获取验证码进行验证后 需要废弃
        this.redisTemplate.delete(redisKey);

        // QueryWrapper 对查询对象进行封装
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        // 根据手机号
        queryWrapper.eq("mobile",phone);

        // selectOne方法  查询单个返回对象
        User user = this.userMapper.selectOne(queryWrapper);

        // 判断，如果根据手机号查询 返回的对象是空 则说明是新用户 注册新用户
        if (user == null){
            user =new User();
            user.setMobile(phone);
            user.setPassword(DigestUtils.md5Hex("123456"));

            // 注册新用户
            this.userMapper.insert(user);
            isNew=true;
        }

        // 生成token
        Map<String,Object> Claims=new HashMap<String,Object>();
        Claims.put("id",user.getId());

        String token= AppJwtUtil.getToken(user.getId());

       /* try {
            // 发送用户成功登的信息
            Map<String,Object> msg=new HashMap<>();
            msg.put("id",user.getId());
            msg.put("date",System.currentTimeMillis());

            this.rocketMQTemplate.convertAndSend("th_sso_login",msg);

        } catch (MessagingException e) {
           log.error("发送消息失败",e);
        }*/

        String data= token + "|" + isNew;
        System.out.println("UserService ... data = " + data);

        // 返回token 和 是否新用户
        return data;
    }
}
