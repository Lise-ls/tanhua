package com.th.dubbo.serverIsImpl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

/*
 * @author Lise
 * @date 2021年05月23日 21:16
 * @program: tanhua
 * @description:
 */

// (exclude= {DataSourceAutoConfiguration.class})  是因为pom文件内，存在mysql的依赖，但是没给mysql的配置文件
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
@EnableAsync //开启异步执行的支持
public class DubboServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DubboServiceApplication.class, args);
    }
}
