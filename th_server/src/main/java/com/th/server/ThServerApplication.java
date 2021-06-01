package com.th.server;

/*
 * @author Lise
 * @date 2021年05月23日 20:57
 * @program: tanhua
 * @description:
 */

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@MapperScan("com.th.common.mapper")
@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@ComponentScan(basePackages={"com.th"}) //设置扫描包范围
public class ThServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThServerApplication.class,args);
    }
}
