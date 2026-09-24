package com.bns.fsl.eft;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EftRtpEftApplication {
    public static void main(String[] args) {
        SpringApplication.run(EftRtpEftApplication.class, args);
    }
}
