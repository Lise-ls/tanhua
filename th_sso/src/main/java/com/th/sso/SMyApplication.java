package com.th.sso;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/*
 * @author Lise
 * @date 2021年05月20日 20:40
 * @program: tanhua
 * @description:
 */
@MapperScan("com.th.common.mapper")
@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@ComponentScan(basePackages={"com.th"})
public class SMyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SMyApplication.class,args);
    }
}
