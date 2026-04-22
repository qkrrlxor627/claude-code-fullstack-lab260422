package com.banking.domain.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountNumberTest {

    @Nested
    @DisplayName("generate — 계좌번호 채번")
    class Generate {

        @Test
        @DisplayName("정상적인 입력은 Luhn 검증번호 포함 계좌번호를 만든다")
        void 정상_채번() {
            AccountNumber number = AccountNumber.generate("110", "01", 1234567L);

            assertThat(number.value()).matches("^110-01-1234567-\\d$");
        }

        @Test
        @DisplayName("동일 입력은 항상 동일 계좌번호를 생성한다 (결정성)")
        void 결정성() {
            AccountNumber a = AccountNumber.generate("110", "01", 1234567L);
            AccountNumber b = AccountNumber.generate("110", "01", 1234567L);

            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("은행코드가 3자리 숫자가 아니면 예외")
        void 은행코드_형식_오류() {
            assertThatThrownBy(() -> AccountNumber.generate("11", "01", 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("은행코드");
        }

        @Test
        @DisplayName("상품코드가 2자리 숫자가 아니면 예외")
        void 상품코드_형식_오류() {
            assertThatThrownBy(() -> AccountNumber.generate("110", "1", 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("상품코드");
        }

        @Test
        @DisplayName("일련번호가 범위를 벗어나면 예외")
        void 일련번호_범위_오류() {
            assertThatThrownBy(() -> AccountNumber.generate("110", "01", -1L))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> AccountNumber.generate("110", "01", 10_000_000L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("AccountNumber 생성자 — 형식/검증번호 확인")
    class Constructor {

        @Test
        @DisplayName("generate 가 만든 계좌번호는 AccountNumber 로 재구성 가능")
        void 왕복() {
            AccountNumber original = AccountNumber.generate("110", "01", 1234567L);
            AccountNumber rebuilt = new AccountNumber(original.value());

            assertThat(rebuilt).isEqualTo(original);
        }

        @Test
        @DisplayName("형식이 다르면 예외 (하이픈 위치 틀림)")
        void 형식_오류() {
            assertThatThrownBy(() -> new AccountNumber("11001-12345670"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("형식");
        }

        @Test
        @DisplayName("검증번호가 틀리면 예외 (한 자리 오타)")
        void 검증번호_오류() {
            AccountNumber valid = AccountNumber.generate("110", "01", 1234567L);
            // 마지막 검증 자리를 다른 값으로 바꿔 오타 시뮬레이션
            char wrong = (valid.value().charAt(valid.value().length() - 1) == '0') ? '1' : '0';
            String corrupted = valid.value().substring(0, valid.value().length() - 1) + wrong;

            assertThatThrownBy(() -> new AccountNumber(corrupted))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("검증번호");
        }

        @Test
        @DisplayName("null 입력 예외")
        void null_예외() {
            assertThatThrownBy(() -> new AccountNumber(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
