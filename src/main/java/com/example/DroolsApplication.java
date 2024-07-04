package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.drools.dao")
public class DroolsApplication {

    protected static Logger logger = LoggerFactory.
            getLogger(DroolsApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DroolsApplication.class, args);
        logger.info("SpringBoot Start Success");
    }
}
