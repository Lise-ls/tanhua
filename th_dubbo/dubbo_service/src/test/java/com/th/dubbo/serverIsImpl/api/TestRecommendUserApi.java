package com.th.dubbo.serverIsImpl.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/*
 * @author Lise
 * @date 2021年05月24日 18:03
 * @program: tanhua
 * @description:
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRecommendUserApi {

    @Resource
    private RecommendUserApi recommendUserApi;


    @Test
    public void testQueryWithMaxScore(){
        System.out.println("recommendUserApi = " + recommendUserApi);

        System.out.println(this.recommendUserApi.queryWithMaxScore(1L));
        System.out.println(this.recommendUserApi.queryWithMaxScore(8L));
        System.out.println(this.recommendUserApi.queryWithMaxScore(26L));
    }

    @Test
    public void testQueryPageInfo(){
        System.out.println(this.recommendUserApi.queryPageInfo(1L,1,5));
        System.out.println(this.recommendUserApi.queryPageInfo(1L,2,5));
        System.out.println(this.recommendUserApi.queryPageInfo(1L,3,5));
    }

}
