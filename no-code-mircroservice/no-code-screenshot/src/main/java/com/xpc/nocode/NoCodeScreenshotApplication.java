package com.xpc.nocode;


import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class NoCodeScreenshotApplication {
    public static void main(String[] args){
        SpringApplication.run(NoCodeScreenshotApplication.class,args);
    }
}
