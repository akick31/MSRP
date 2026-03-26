import { DailyItem, VerifyRequest, VerifyResponse } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1/msrp';

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${url}`, {
    headers: {
      'Content-Type': 'application/json',
    },
    ...options,
  });

  if (!response.ok) {
    const errorBody = await response.json().catch(() => null);
    const message = errorBody?.error || `Request failed with status ${response.status}`;
    throw new Error(message);
  }

  return response.json();
}

export async function fetchTodayItems(): Promise<DailyItem[]> {
  return request<DailyItem[]>('/today');
}

export async function verifyGuess(payload: VerifyRequest): Promise<VerifyResponse> {
  return request<VerifyResponse>('/verify', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}
