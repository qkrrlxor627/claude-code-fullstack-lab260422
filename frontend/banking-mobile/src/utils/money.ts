import BigNumber from 'bignumber.js';

BigNumber.config({ DECIMAL_PLACES: 2, ROUNDING_MODE: BigNumber.ROUND_HALF_UP });

export function formatKRW(value: string | null | undefined): string {
  if (value == null || value === '') return '₩0.00';
  const bn = new BigNumber(value);
  if (bn.isNaN()) return value;
  return `₩${bn.toFormat(2)}`;
}

export type AmountValidation =
  | { ok: true; value: string }
  | { ok: false; reason: string };

const AMOUNT_PATTERN = /^\d+(\.\d{1,2})?$/;

export function validateAmount(input: string): AmountValidation {
  const trimmed = input.trim();
  if (trimmed === '') {
    return { ok: false, reason: '금액을 입력해주세요' };
  }
  if (!AMOUNT_PATTERN.test(trimmed)) {
    return { ok: false, reason: '숫자와 소수점 2자리까지만 입력 가능합니다' };
  }
  const bn = new BigNumber(trimmed);
  if (bn.isNaN()) {
    return { ok: false, reason: '숫자 형식이 올바르지 않습니다' };
  }
  if (bn.isLessThanOrEqualTo(0)) {
    return { ok: false, reason: '0보다 큰 금액을 입력해주세요' };
  }
  return { ok: true, value: bn.toFixed(2) };
}
