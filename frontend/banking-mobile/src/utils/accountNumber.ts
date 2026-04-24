const FORMAT_PATTERN = /^\d{3}-\d{2}-\d{7}-[0-9X]$/;

export function looksLikeAccountNumber(input: string): boolean {
  return FORMAT_PATTERN.test(input.trim());
}

export function normalizeAccountNumberInput(input: string): string {
  return input.trim().toUpperCase();
}
