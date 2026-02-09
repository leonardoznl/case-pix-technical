package com.example.pix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PixApplication {
    public static void main(String[] args) {
        SpringApplication.run(PixApplication.class, args);
    }
}
