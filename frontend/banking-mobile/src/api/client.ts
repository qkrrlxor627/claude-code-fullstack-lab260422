import axios, { AxiosError, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import { ApiEnvelope, ApiError } from './types';

const baseURL = process.env.EXPO_PUBLIC_API_BASE_URL ?? 'http://localhost:8080/api/v1';

export const apiClient = axios.create({
  baseURL,
  timeout: 10_000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
});

apiClient.interceptors.response.use(
  (response: AxiosResponse<ApiEnvelope<unknown>>) => {
    const envelope = response.data;
    if (envelope && typeof envelope === 'object' && 'success' in envelope) {
      if (!envelope.success) {
        const code = envelope.error?.code ?? 'UNKNOWN';
        const message = envelope.error?.message ?? '알 수 없는 오류가 발생했습니다';
        throw new ApiError(code, message, response.status);
      }
      response.data = envelope.data as unknown as ApiEnvelope<unknown>;
    }
    return response;
  },
  (error: AxiosError<ApiEnvelope<unknown>>) => {
    if (error.response?.data && typeof error.response.data === 'object' && 'success' in error.response.data) {
      const envelope = error.response.data;
      const code = envelope.error?.code ?? 'UNKNOWN';
      const message = envelope.error?.message ?? error.message ?? '요청이 실패했습니다';
      throw new ApiError(code, message, error.response.status);
    }
    if (error.code === 'ECONNABORTED') {
      throw new ApiError('TIMEOUT', '서버 응답이 지연되고 있습니다', 0);
    }
    if (!error.response) {
      throw new ApiError('NETWORK_ERROR', '서버에 연결할 수 없습니다. 백엔드와 EXPO_PUBLIC_API_BASE_URL 을 확인해주세요.', 0);
    }
    throw new ApiError('HTTP_ERROR', error.message ?? '요청이 실패했습니다', error.response.status);
  },
);

export function getApiBaseUrl(): string {
  return baseURL;
}

export type { InternalAxiosRequestConfig };
