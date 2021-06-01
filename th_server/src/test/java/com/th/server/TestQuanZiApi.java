package com.th.server;


import com.alibaba.dubbo.config.annotation.Reference;
import com.th.dubbo.serverIsImpl.api.QuanZiApi;
import com.th.dubbo.serverIsImpl.pojo.Publish;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/*
 * @author Lise
 * @date 2021年05月26日 20:13
 * @program: th
 * @description:  发布动态测试
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestQuanZiApi {

    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;


    @Test
    public void testSavePublish(){
        Publish publish = new Publish();
        publish.setText("1111,2222,3333,4444");
        publish.setUserId(1L);
        publish.setSeeType(1);
        publish.setLongitude("116.350426");
        publish.setLatitude("40.066355");
        publish.setLocationName("湖北武汉");
        this.quanZiApi.savePublish(publish);
    }

}
