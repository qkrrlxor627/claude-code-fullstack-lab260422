package com.banking.account;

import com.banking.account.domain.Account;
import com.banking.account.domain.AccountNumber;
import com.banking.account.AccountService;
import com.banking.account.domain.AccountStatus;
import com.banking.account.domain.AccountType;
import com.banking.account.exception.AccountAlreadyClosedException;
import com.banking.account.exception.AccountNotFoundException;
import com.banking.account.exception.InsufficientBalanceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AccountService accountService;

    private static final AccountNumber NUMBER = AccountNumber.generate("110", "01", 1000000L);

    @Test
    @DisplayName("POST /api/v1/accounts — 정상 개설은 201 + ApiResponse success")
    void 개설_성공() throws Exception {
        Account saved = Account.reconstitute(
                1L, NUMBER, "김싸피", AccountType.SAVINGS, AccountStatus.ACTIVE,
                BigDecimal.ZERO, Instant.parse("2026-04-22T00:00:00Z"), null);
        given(accountService.open(anyString(), any(AccountType.class))).willReturn(saved);

        String body = objectMapper.writeValueAsString(Map.of(
                "holder", "김싸피",
                "type", "SAVINGS"
        ));

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value(NUMBER.value()))
                .andExpect(jsonPath("$.data.holder").value("김싸피"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.balance").value("0.00"));

        ArgumentCaptor<String> holderCaptor = ArgumentCaptor.forClass(String.class);
        verify(accountService).open(holderCaptor.capture(), any(AccountType.class));
        assertThat(holderCaptor.getValue()).isEqualTo("김싸피");
    }

    @Test
    @DisplayName("POST /api/v1/accounts — holder 공백이면 400")
    void 개설_검증_실패() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "holder", "",
                "type", "SAVINGS"
        ));

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{number} — 존재하지 않으면 400 + 에러 코드")
    void 조회_없음() throws Exception {
        given(accountService.find(any(AccountNumber.class)))
                .willThrow(new AccountNotFoundException(NUMBER));

        mockMvc.perform(get("/api/v1/accounts/" + NUMBER.value()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ACCOUNT_NOT_FOUND"));
    }

    @Test
    @DisplayName("DELETE /api/v1/accounts/{number} — 해지 성공")
    void 해지_성공() throws Exception {
        Account closed = Account.reconstitute(
                1L, NUMBER, "김싸피", AccountType.SAVINGS, AccountStatus.CLOSED,
                BigDecimal.ZERO, Instant.parse("2026-04-01T00:00:00Z"),
                Instant.parse("2026-04-22T00:00:00Z"));
        given(accountService.close(any(AccountNumber.class))).willReturn(closed);

        mockMvc.perform(delete("/api/v1/accounts/" + NUMBER.value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CLOSED"))
                .andExpect(jsonPath("$.data.closedAt").exists());
    }

    @Test
    @DisplayName("DELETE /api/v1/accounts/{number} — 이미 CLOSED 면 400")
    void 해지_이미_CLOSED() throws Exception {
        given(accountService.close(any(AccountNumber.class)))
                .willThrow(new AccountAlreadyClosedException(NUMBER));

        mockMvc.perform(delete("/api/v1/accounts/" + NUMBER.value()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ACCOUNT_ALREADY_CLOSED"));
    }

    @Test
    @DisplayName("POST /api/v1/accounts/{number}/deposits — 정상 입금은 200 + 증가된 balance")
    void 입금_성공() throws Exception {
        Account deposited = Account.reconstitute(
                1L, NUMBER, "김싸피", AccountType.SAVINGS, AccountStatus.ACTIVE,
                new BigDecimal("10000.00"),
                Instant.parse("2026-04-22T00:00:00Z"), null);
        given(accountService.deposit(any(AccountNumber.class), any(BigDecimal.class)))
                .willReturn(deposited);

        String body = objectMapper.writeValueAsString(Map.of("amount", "10000.00"));

        mockMvc.perform(post("/api/v1/accounts/" + NUMBER.value() + "/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balance").value("10000.00"));

        ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(accountService).deposit(any(AccountNumber.class), amountCaptor.capture());
        assertThat(amountCaptor.getValue()).isEqualByComparingTo("10000.00");
    }

    @Test
    @DisplayName("POST /api/v1/accounts/{number}/deposits — 숫자가 아닌 amount 는 400")
    void 입금_금액_검증_실패() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("amount", "abc"));

        mockMvc.perform(post("/api/v1/accounts/" + NUMBER.value() + "/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/v1/accounts/{number}/withdrawals — 정상 출금은 200 + 감소된 balance")
    void 출금_성공() throws Exception {
        Account withdrawn = Account.reconstitute(
                1L, NUMBER, "김싸피", AccountType.SAVINGS, AccountStatus.ACTIVE,
                new BigDecimal("7000.00"),
                Instant.parse("2026-04-22T00:00:00Z"), null);
        given(accountService.withdraw(any(AccountNumber.class), any(BigDecimal.class)))
                .willReturn(withdrawn);

        String body = objectMapper.writeValueAsString(Map.of("amount", "3000.00"));

        mockMvc.perform(post("/api/v1/accounts/" + NUMBER.value() + "/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balance").value("7000.00"));

        ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(accountService).withdraw(any(AccountNumber.class), amountCaptor.capture());
        assertThat(amountCaptor.getValue()).isEqualByComparingTo("3000.00");
    }

    @Test
    @DisplayName("POST /api/v1/accounts/{number}/withdrawals — 잔액 부족이면 400 + INSUFFICIENT_BALANCE")
    void 출금_잔액_부족() throws Exception {
        given(accountService.withdraw(any(AccountNumber.class), any(BigDecimal.class)))
                .willThrow(new InsufficientBalanceException(
                        NUMBER, new BigDecimal("10000.00"), new BigDecimal("5000.00")));

        String body = objectMapper.writeValueAsString(Map.of("amount", "10000.00"));

        mockMvc.perform(post("/api/v1/accounts/" + NUMBER.value() + "/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INSUFFICIENT_BALANCE"));
    }

    @Test
    @DisplayName("POST /api/v1/accounts/{number}/withdrawals — 숫자가 아닌 amount 는 400")
    void 출금_금액_검증_실패() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("amount", "abc"));

        mockMvc.perform(post("/api/v1/accounts/" + NUMBER.value() + "/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
}
