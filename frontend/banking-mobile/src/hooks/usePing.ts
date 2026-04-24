import { useQuery } from '@tanstack/react-query';
import { ping } from '@/api/ping';
import { ApiError, PingResponse } from '@/api/types';

export function usePing() {
  return useQuery<PingResponse, ApiError>({
    queryKey: ['ping'],
    queryFn: ping,
    refetchInterval: 15_000,
    retry: 0,
  });
}
