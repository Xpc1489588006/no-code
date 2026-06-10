package com.xpc.nocode;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.xpc.nocode.mapper")
public class NoCodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoCodeApplication.class, args);
    }

}
