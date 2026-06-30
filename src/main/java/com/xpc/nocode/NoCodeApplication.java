package com.xpc.nocode;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.xpc.nocode.mapper")
@EnableCaching
public class NoCodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoCodeApplication.class, args);
    }

}
