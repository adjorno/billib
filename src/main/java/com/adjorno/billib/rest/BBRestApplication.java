package com.adjorno.billib.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync
@SpringBootApplication
public class BBRestApplication {
    public static final String PASSWORD = "vtldtlm";

    public static void main(String[] args) {
        SpringApplication.run(BBRestApplication.class, args);
    }
}
