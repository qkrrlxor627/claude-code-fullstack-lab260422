package com.banking.infra.persistence;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * infra 모듈의 JPA 설정 (EntityScan / EnableJpaRepositories).
 * api 모듈의 BankingApplication 이 infra 의 영속성 내부 구조를 몰라도 되게 분리.
 * @SpringBootApplication(scanBasePackages = "com.banking") 가 이 설정을 자동 감지한다.
 */
@Configuration
@EntityScan(basePackages = "com.banking.infra.persistence")
@EnableJpaRepositories(basePackages = "com.banking.infra.persistence")
public class PersistenceConfig {
}
