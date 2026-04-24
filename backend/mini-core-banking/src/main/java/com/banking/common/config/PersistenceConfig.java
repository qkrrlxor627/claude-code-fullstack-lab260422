package com.banking.common.config;

import org.springframework.context.annotation.Configuration;

/**
 * JPA 설정 placeholder.
 * 과거 레이어드 구조에서는 `@EntityScan("com.banking.entity")` /
 * `@EnableJpaRepositories("com.banking.repository")` 로 스캔 범위를 한정했으나,
 * vertical slice 전환 후에는 각 feature 가 자체 `infra/` 패키지에 JPA 엔티티/리포지토리를 둔다.
 * `BankingApplication` 의 `@SpringBootApplication` 기본 스캔이 `com.banking` 하위 전체를
 * 커버하므로 별도 범위 지정 불필요.
 */
@Configuration
public class PersistenceConfig {
}
