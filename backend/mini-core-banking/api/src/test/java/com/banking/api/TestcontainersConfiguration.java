package com.banking.api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * @SpringBootTest 에서 실제 PostgreSQL 을 Testcontainers 로 기동.
 * @ServiceConnection 이 datasource url/username/password 를 자동으로 주입.
 * Flyway 는 정상적으로 실행되어 스키마/시퀀스를 만든다.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
    }
}
