package com.th.dubbo.server;

/*
 * @author Lise
 * @date 2021年05月31日 20:08
 * @program: tanhua
 */

import com.th.dubbo.server.service.RetryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRetryService {

    @Autowired
    private RetryService retryService;

    @Test
    public void testRetry() {
        System.out.println(this.retryService.execute(90));
    }
}
