package com.banking.api;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * ApplicationContext 스모크 테스트.
 *
 * TODO(docker-compat): Testcontainers 1.19.8/1.20.4 + Docker 29.1.3 조합에서
 *  /info 엔드포인트 응답이 status 400 으로 떨어지며 Docker 환경 감지가 실패한다.
 *  (desktop-linux 컨텍스트, npipe:////./pipe/dockerDesktopLinuxEngine 지정에도 동일.)
 *  Step 2-2 동시성 테스트 진입 전 해결 과제. 후보:
 *   - Testcontainers 1.21+ 로 승격
 *   - Docker Desktop 설정에서 TCP 엔드포인트 노출 후 DOCKER_HOST=tcp://localhost:2375
 *   - Ryuk 비활성화 (TESTCONTAINERS_RYUK_DISABLED=true)
 *  임시로는 docker-compose 의 PostgreSQL 을 직접 띄워 수동 검증으로 대체.
 */
@Disabled("Docker 29.x + Testcontainers 호환 이슈 — Step 2-2 전에 해결 예정")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class BankingApplicationTests {

    @Test
    void contextLoads() {
    }
}
