package com.tripdog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.tripdog.mapper")
public class TripdogBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TripdogBackendApplication.class, args);
    }

}
