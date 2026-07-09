import { DailyItem, GlobalStats, VerifyRequest, VerifyResponse } from '../types';

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${url}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });

  if (!response.ok) {
    const errorBody = await response.json().catch(() => null);
    const message = errorBody?.error || `Request failed with status ${response.status}`;
    throw new Error(message);
  }

  return response.json();
}

export async function fetchTodayItems(date?: string): Promise<DailyItem[]> {
  const d = date ?? new Date().toLocaleDateString('en-CA');
  return request<DailyItem[]>(`/game/today?date=${d}`);
}

export async function verifyGuess(payload: VerifyRequest): Promise<VerifyResponse> {
  return request<VerifyResponse>('/game/verify', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export async function fetchAvailableDates(): Promise<string[]> {
  return request<string[]>('/game/available-dates');
}

export async function recordAnalytics(eventType: string): Promise<void> {
  try {
    await fetch(`${API_BASE_URL}/analytics`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ eventType }),
    });
  } catch (e) {
    console.error('[analytics] failed to record', eventType, e);
  }
}

export async function submitScore(score: number, date: string): Promise<void> {
  try {
    await fetch(`${API_BASE_URL}/analytics/score`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ score, date }),
    });
  } catch (e) {
    console.error('failed to submit score', e);
  }
}

export async function fetchGlobalStats(date: string): Promise<GlobalStats> {
  return request<GlobalStats>(`/analytics/game-stats?date=${date}`);
}

export async function submitContact(payload: {
  name: string;
  email: string;
  subject: string;
  message: string;
}): Promise<void> {
  await request('/contact', { method: 'POST', body: JSON.stringify(payload) });
}
