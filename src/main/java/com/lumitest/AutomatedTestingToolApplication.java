package com.lumitest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AutomatedTestingToolApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutomatedTestingToolApplication.class, args);
    }
}
