package com.ecode.modelevalplat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ModelEvalPlatApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModelEvalPlatApplication.class, args);
    }
}