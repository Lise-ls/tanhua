package com.th.sso.controller;

/*
 * @author Lise
 * @date 2021年05月21日 21:08
 * @program: th
 * @description:
 */


import com.th.sso.service.UserService;
import com.th.sso.vo.ErrorResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("user")
public class UserController {

        @Resource
        private UserService userService;

        @PostMapping("loginVerification")
        public ResponseEntity<Object> login(@RequestBody Map<String,String> param){
                try {
                    // 获取手机号和验证码
                    String phone = param.get("phone");
                    String code = param.get("verificationCode");


                    String data = this.userService.login(phone, code);
                    // 判断，如果不为空，则说明登录成功
                    if (StringUtils.isNotEmpty(data)) {
                        // 打印
                        System.out.println("data = " + data);
                        // 登录
                        Map<String, Object> result = new HashMap<String, Object>(2);
                        String[] split = StringUtils.split(data, "|");

                        result.put("token", split[0]);
                        result.put("isNew", Boolean.valueOf(split[1]));

                        return ResponseEntity.ok(result);
                    }
            } catch (Exception e) {
                e.printStackTrace();
            }

            ErrorResult errorResult = ErrorResult.builder().errCode("000002").errMessage("登录失败！").build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }

}
