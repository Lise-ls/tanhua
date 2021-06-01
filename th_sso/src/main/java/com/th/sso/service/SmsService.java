package com.th.sso.service;


import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.th.sso.config.AliyunSMSConfig;
import com.th.sso.vo.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/*
 * @author Lise
 * @date 2021年05月20日 21:27
 * @program: th
 * @description:
 */
@Service
@Slf4j
public class SmsService {

    @Autowired
    private AliyunSMSConfig aliyunSMSConfig;


    @Autowired
    private RedisTemplate<String,String> redisTemplate;


    public String sendSems(String mobile){

        DefaultProfile profile = DefaultProfile.getProfile(this.aliyunSMSConfig.getRegionId(),
                this.aliyunSMSConfig.getAccessKeyId(), this.aliyunSMSConfig.getAccessKeySecret());

        IAcsClient client = new DefaultAcsClient(profile);
        //获取随机的验证码
        String code = RandomUtils.nextInt(100000, 999999) + "";

        // 创建API请求并设置参数
        CommonRequest request = new CommonRequest();

        // 请求方式
        request.setSysMethod(MethodType.POST);

        // 阿里云短信地址
        request.setSysDomain(this.aliyunSMSConfig.getDomain());


        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", this.aliyunSMSConfig.getRegionId());
        request.putQueryParameter("PhoneNumbers", mobile); //目标手机号
        request.putQueryParameter("SignName","李登攀");
        //request.putQueryParameter("SignName", this.aliyunSMSConfig.getSignName()); //签名名称
        request.putQueryParameter("TemplateCode", this.aliyunSMSConfig.getTemplateCode()); //短信模板code
        request.putQueryParameter("TemplateParam", "{\"code\":\"" + code + "\"}");//模板中变量替换
        try {
            CommonResponse response = client.getCommonResponse(request);
            //
            String data = response.getData();
            System.out.println("data = " + data);

            if (StringUtils.contains(data, "\"Message\":\"OK\"")) {
                return code;
            }
            log.info("发送短信验证码失败~ data = " + data);
        } catch (Exception e) {
            log.error("发送短信验证码失败~ mobile = " + mobile, e);
        }
        return null;
    }





    public ErrorResult sendChecked(String phone) {

        // 拿到手机号
        String redisKey="CHECK_CODE_"+phone;
        System.out.println("redisKey = " + redisKey);


        //调用发送短信方法，传入手机号，得到验证码
        //String code = this.sendSems(phone);


        String code="123456";
        System.out.println("code = " + code);


        //短信发送成功，将验证码保存到redis中，有效期为5分钟
        this.redisTemplate.opsForValue().set(redisKey, code, Duration.ofMinutes(5));

        return null;
    }
}
