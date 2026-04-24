package com.banking.account.infra;

import com.banking.account.domain.Account;
import com.banking.account.domain.AccountNumber;
import com.banking.account.domain.AccountStatus;
import com.banking.account.domain.AccountType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("Docker 29.x + Testcontainers 호환 이슈 — Step 2-2 전에 해결 예정. " +
        "임시 수동 검증: docker compose up -d postgres + gradlew :api:bootRun + Swagger UI")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Import(AccountRepositoryAdapter.class)
@Testcontainers
class AccountRepositoryAdapterIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    AccountRepositoryAdapter adapter;

    private static final Clock FIXED =
            Clock.fixed(Instant.parse("2026-04-22T00:00:00Z"), ZoneId.of("UTC"));

    @Test
    @DisplayName("save — 신규 저장 후 id 가 할당된다")
    void save_신규() {
        long seq = adapter.nextSequence();
        AccountNumber number = AccountNumber.generate("110", "01", seq);
        Account account = Account.open(number, "김싸피", AccountType.SAVINGS, FIXED);

        Account saved = adapter.save(account);

        assertThat(saved.id()).isNotNull();
    }

    @Test
    @DisplayName("findByNumber — 저장 후 도메인 객체로 재구성된다 (Mapper 왕복)")
    void findByNumber_왕복() {
        long seq = adapter.nextSequence();
        AccountNumber number = AccountNumber.generate("110", "01", seq);
        Account account = Account.open(number, "이테스트", AccountType.FIXED_DEPOSIT, FIXED);
        adapter.save(account);

        Optional<Account> loaded = adapter.findByNumber(number);

        assertThat(loaded).isPresent();
        assertThat(loaded.get().accountNumber()).isEqualTo(number);
        assertThat(loaded.get().holder()).isEqualTo("이테스트");
        assertThat(loaded.get().type()).isEqualTo(AccountType.FIXED_DEPOSIT);
        assertThat(loaded.get().status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(loaded.get().balance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("existsByNumber — 저장 여부 확인")
    void existsByNumber() {
        long seq = adapter.nextSequence();
        AccountNumber number = AccountNumber.generate("110", "01", seq);

        assertThat(adapter.existsByNumber(number)).isFalse();

        adapter.save(Account.open(number, "존재테스트", AccountType.SAVINGS, FIXED));

        assertThat(adapter.existsByNumber(number)).isTrue();
    }

    @Test
    @DisplayName("nextSequence — 호출할 때마다 단조 증가")
    void nextSequence_단조증가() {
        long a = adapter.nextSequence();
        long b = adapter.nextSequence();
        long c = adapter.nextSequence();

        assertThat(b).isGreaterThan(a);
        assertThat(c).isGreaterThan(b);
    }
}
