package com.th.sso;

import com.th.sso.service.SmsService;
import com.th.sso.vo.ErrorResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/*
 * @author Lise
 * @date 2021年05月29日 11:12
 * @program: tanhua
 * @description:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class STest {

    @Autowired
    private SmsService smsService;

    @Test
    public void test(){
        ErrorResult errorResult = smsService.sendChecked("17602026868");
        System.out.println(errorResult);
    }
}
