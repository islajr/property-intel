import client from './client';
import type { Alert, AlertRequest } from '../types/api';

export const list = async (): Promise<Alert[]> => {
  const response = await client.get<Alert[]>('/alerts');
  return response.data;
};

export const create = async (body: AlertRequest, idempotencyKey: string): Promise<Alert> => {
  const response = await client.post<Alert>('/alerts', body, {
    headers: {
      'Idempotency-Key': idempotencyKey,
    },
  });
  return response.data;
};

export const remove = async (id: number): Promise<void> => {
  await client.delete(`/alerts/${id}`);
};

export const toKobo = (naira: number): number => Math.round(naira * 100);
