package com.banking.transfer;

import com.banking.transfer.dto.TransferResponse;
import com.banking.account.domain.Account;
import com.banking.account.domain.AccountNumber;
import com.banking.transaction.domain.Transaction;
import com.banking.transaction.domain.TransactionType;
import com.banking.transaction.event.TransactionCompletedEvent;
import com.banking.account.exception.AccountNotFoundException;
import com.banking.transfer.exception.IdempotencyConflictException;
import com.banking.transfer.exception.SelfTransferException;
import com.banking.transfer.idempotency.IdempotencyStore;
import com.banking.account.port.AccountRepository;
import com.banking.transaction.port.TransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * 이체 도메인 서비스.
 * <p>학습 포인트:
 * <ul>
 *   <li>두 계좌 락을 accountNumber 오름차순으로 획득 → 데드락 방지</li>
 *   <li>X-Idempotency-Key 헤더로 같은 요청 재전송 안전성 보장</li>
 *   <li>OUT/IN 두 Transaction 레코드에 transferId(uuid) 로 그룹핑</li>
 * </ul>
 */
@Service
@Transactional
public class TransferService {

    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);
    private static final String KEY_PREFIX = "idem:transfer:";

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final IdempotencyStore idempotencyStore;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public TransferService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            IdempotencyStore idempotencyStore,
            ApplicationEventPublisher eventPublisher,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.idempotencyStore = idempotencyStore;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public TransferResponse transfer(AccountNumber from, AccountNumber to,
                                     BigDecimal amount, String idempotencyKey) {

        // 1) 멱등성 캐시 조회 — 히트하면 기존 응답 그대로
        String cacheKey = KEY_PREFIX + idempotencyKey;
        Optional<String> cached = idempotencyStore.find(cacheKey);
        if (cached.isPresent()) {
            try {
                return objectMapper.readValue(cached.get(), TransferResponse.class);
            } catch (JsonProcessingException ex) {
                throw new IdempotencyConflictException(idempotencyKey, "저장 payload 역직렬화 실패");
            }
        }

        // 2) 자기 자신 이체 금지
        if (from.equals(to)) {
            throw new SelfTransferException(from);
        }

        // 3) 락 순서 — accountNumber 오름차순
        boolean fromFirst = from.value().compareTo(to.value()) < 0;
        AccountNumber firstLock = fromFirst ? from : to;
        AccountNumber secondLock = fromFirst ? to : from;

        Account firstAcc = accountRepository.findByNumberForUpdate(firstLock)
                .orElseThrow(() -> new AccountNotFoundException(firstLock));
        Account secondAcc = accountRepository.findByNumberForUpdate(secondLock)
                .orElseThrow(() -> new AccountNotFoundException(secondLock));

        Account fromAccount = fromFirst ? firstAcc : secondAcc;
        Account toAccount = fromFirst ? secondAcc : firstAcc;

        // 4) 도메인 규칙 실행
        fromAccount.withdraw(amount);
        toAccount.deposit(amount);

        // 5) 계좌 저장
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // 6) Transaction 2건 기록 (공통 transferId 로 묶음) + 원장 이벤트 발행
        String transferId = UUID.randomUUID().toString();
        Transaction outTx = transactionRepository.save(Transaction.transfer(
                fromAccount.accountNumber(), TransactionType.TRANSFER_OUT, amount,
                toAccount.accountNumber(), transferId, fromAccount.balance(), clock));
        Transaction inTx = transactionRepository.save(Transaction.transfer(
                toAccount.accountNumber(), TransactionType.TRANSFER_IN, amount,
                fromAccount.accountNumber(), transferId, toAccount.balance(), clock));

        eventPublisher.publishEvent(new TransactionCompletedEvent(
                outTx.id(), outTx.accountNumber().value(), outTx.type(),
                outTx.amount(), outTx.counterparty().value(), outTx.transferId()));
        eventPublisher.publishEvent(new TransactionCompletedEvent(
                inTx.id(), inTx.accountNumber().value(), inTx.type(),
                inTx.amount(), inTx.counterparty().value(), inTx.transferId()));

        // 7) 응답 구성 + 멱등성 캐시 저장
        TransferResponse response = new TransferResponse(
                transferId,
                fromAccount.accountNumber().value(),
                toAccount.accountNumber().value(),
                amount,
                fromAccount.balance(),
                toAccount.balance(),
                clock.instant()
        );

        try {
            idempotencyStore.putIfAbsent(
                    cacheKey,
                    objectMapper.writeValueAsString(response),
                    IDEMPOTENCY_TTL);
        } catch (JsonProcessingException ex) {
            // 직렬화 실패해도 DB 는 이미 커밋 예정 — 재시도 시엔 캐시 미스로 다시 실행될 수 있음.
            // 토이 수준에서는 로깅만. 실제 시스템에서는 retry-safe 저장 계층으로 교체 필요.
        }

        return response;
    }
}
