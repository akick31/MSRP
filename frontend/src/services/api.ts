import { DailyItem, GlobalStats, VerifyRequest, VerifyResponse } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1/msrp';

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
  const d = date ?? new Date().toLocaleDateString('en-CA', { timeZone: 'America/New_York' });
  return request<DailyItem[]>(`/today?date=${d}`);
}

export async function verifyGuess(payload: VerifyRequest): Promise<VerifyResponse> {
  return request<VerifyResponse>('/verify', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export async function fetchAvailableDates(): Promise<string[]> {
  return request<string[]>('/available-dates');
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
    await fetch(`${API_BASE_URL}/score`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ score, date }),
    });
  } catch (e) {
    console.error('[analytics] failed to submit score', e);
  }
}

export async function fetchGlobalStats(date: string): Promise<GlobalStats> {
  return request<GlobalStats>(`/game-stats?date=${date}`);
}

export async function submitContact(payload: {
  name: string;
  email: string;
  subject: string;
  message: string;
}): Promise<void> {
  const res = await fetch(`${API_BASE_URL}/contact`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error((body as { error?: string }).error ?? `HTTP ${res.status}`);
  }
}
