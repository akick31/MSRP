import { useState, useCallback } from 'react';
import { PlayerStats } from '../types';

const STATS_KEY = 'msrp-stats';

function getLocalDate(date: Date): string {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

function getToday(): string {
  return getLocalDate(new Date());
}

function getYesterday(): string {
  const d = new Date();
  d.setDate(d.getDate() - 1);
  return getLocalDate(d);
}

function loadStats(): PlayerStats {
  try {
    const raw = localStorage.getItem(STATS_KEY);
    if (raw) {
      return JSON.parse(raw);
    }
  } catch {
    // corrupted data, reset
  }
  return {
    currentStreak: 0,
    maxStreak: 0,
    gamesPlayed: 0,
    lastPlayedDate: '',
  };
}

function saveStats(stats: PlayerStats): void {
  localStorage.setItem(STATS_KEY, JSON.stringify(stats));
}

export function useStats() {
  const [stats, setStats] = useState<PlayerStats>(loadStats);

  const recordGame = useCallback(() => {
    const today = getToday();
    const current = loadStats();

    if (current.lastPlayedDate === today) {
      return current;
    }

    let newStreak: number;
    if (current.lastPlayedDate === getYesterday()) {
      newStreak = current.currentStreak + 1;
    } else {
      newStreak = 1;
    }

    const updated: PlayerStats = {
      currentStreak: newStreak,
      maxStreak: Math.max(current.maxStreak, newStreak),
      gamesPlayed: current.gamesPlayed + 1,
      lastPlayedDate: today,
    };

    saveStats(updated);
    setStats(updated);
    return updated;
  }, []);

  return { stats, recordGame };
}
