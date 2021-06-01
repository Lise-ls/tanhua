package com.th.sso.controller;

/*
 * @author Lise
 * @date 2021年05月22日 13:55
 * @program: th
 * @description:
 */

import com.th.sso.service.UserInfoService;
import com.th.sso.vo.ErrorResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("user")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;


    //完善个人信息
    @PostMapping("loginReginfo")
    public ResponseEntity<Object> saveUserInfo(@RequestBody Map<String,String > param,
                                               @RequestHeader("Authorization")String token){

        System.out.println(" 进入填写资料------ ");

        try {
            Boolean bool=this.userInfoService.saveUserInfo(param,token);
            if (bool){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ErrorResult errorResult= ErrorResult.builder().errCode("000001").errMessage("保存用户信息失败！").build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
    }


    //完善个人信息-用户头像处理
    @PostMapping("/loginReginfo/head")
    public ResponseEntity<Object> saveUserLogo(@RequestParam("headPhoto") MultipartFile file,
                                               @RequestHeader("Authorization") String token) {

        System.out.println(" 进入头像上传------ ");

        try {
            //String authorization = map.get("Authorization");
            System.out.println("token = " + token);
            Boolean bool = this.userInfoService.saveUserLogo(file, token);
            if (bool) {
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ErrorResult errorResult = ErrorResult.builder().errCode("000001").errMessage("保存用户logo失败！").build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
    }
}
