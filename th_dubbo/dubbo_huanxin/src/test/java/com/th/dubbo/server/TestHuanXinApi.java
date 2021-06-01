package com.th.dubbo.server;

/*
 * @author Lise
 * @date 2021年05月31日 18:05
 * @program: tanhua
 */

import com.th.dubbo.serverIsImpl.api.HuanXinApi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestHuanXinApi {

    @Resource
    private HuanXinApi huanXinApi;

    @Test
    public void testGetToken(){
        String token = this.huanXinApi.getToken();
        System.out.println("token: "  +token);
    }


    @Test
    public void testRegister(){
        //注册用户id为1的用户到环信
        System.out.println(this.huanXinApi.register(1L));
    }

    @Test
    public void testQueryHuanXinUser(){
        //根据用户id查询环信用户信息
        System.out.println(this.huanXinApi.queryHuanXinUser(1L));
    }
}
