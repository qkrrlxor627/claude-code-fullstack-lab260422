package com.banking.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.ZoneId;

@SpringBootApplication(scanBasePackages = "com.banking")
public class BankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingApplication.class, args);
    }

    /**
     * 도메인 서비스에서 시간 주입 받을 수 있도록 Clock 을 빈으로 등록.
     * 테스트에서는 고정 Clock 을 재정의할 수 있음.
     */
    @Bean
    public Clock systemClock() {
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }
}
