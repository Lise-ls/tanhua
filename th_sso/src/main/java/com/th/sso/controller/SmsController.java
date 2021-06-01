package com.th.sso.controller;

import com.th.sso.service.SmsService;
import com.th.sso.vo.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/*
 * @author Lise
 * @date 2021年05月20日 21:40
 * @program: th
 * @description:
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class SmsController {

    @Autowired
    private SmsService smsService;


    @PostMapping("/login")
    public ResponseEntity<ErrorResult> sendChecked(@RequestBody Map<String,String> param){

        System.out.println("手机号登录运行了...");
        // 错误信息对象
        ErrorResult errorResult=null;

        String phone = param.get("phone");
        try{
            errorResult=this.smsService.sendChecked(phone);
            if (errorResult==null){
                return ResponseEntity.ok(null);
            }
        }catch (Exception e){
            log.error("短信发送失败"+phone,e);
            ErrorResult.builder().errCode("000002").errMessage("短信发送失败").build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
    }
}
