import { useState, useEffect, useCallback } from 'react';
import { DailyItem, RoundResult, GameState, GameProgress } from '../types';
import { fetchTodayItems, verifyGuess } from '../services/api';

const PROGRESS_KEY = 'msrp-progress';
const TOTAL_ROUNDS = 5;

function getToday(): string {
  return new Date().toISOString().split('T')[0];
}

function loadProgress(): GameProgress | null {
  try {
    const raw = localStorage.getItem(PROGRESS_KEY);
    if (raw) {
      const progress: GameProgress = JSON.parse(raw);
      if (progress.date === getToday()) {
        return progress;
      }
    }
  } catch {
    // corrupted
  }
  return null;
}

function saveProgress(progress: GameProgress): void {
  localStorage.setItem(PROGRESS_KEY, JSON.stringify(progress));
}

export function useGameState() {
  const [items, setItems] = useState<DailyItem[]>([]);
  const [currentRound, setCurrentRound] = useState(0);
  const [results, setResults] = useState<RoundResult[]>([]);
  const [gameState, setGameState] = useState<GameState>('loading');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function init() {
      try {
        const todayItems = await fetchTodayItems();
        if (cancelled) return;

        setItems(todayItems);

        const saved = loadProgress();
        if (saved && saved.results.length > 0) {
          setResults(saved.results);
          if (saved.currentRound >= TOTAL_ROUNDS) {
            setCurrentRound(TOTAL_ROUNDS);
            setGameState('finished');
          } else {
            setCurrentRound(saved.currentRound);
            setGameState('playing');
          }
        } else {
          setGameState('playing');
        }
      } catch (e) {
        if (cancelled) return;
        setError(e instanceof Error ? e.message : 'Failed to load items');
        setGameState('loading');
      }
    }

    init();
    return () => { cancelled = true; };
  }, []);

  const submitGuess = useCallback(async (guess: number) => {
    if (gameState !== 'playing' || currentRound >= items.length) return;

    const item = items[currentRound];

    try {
      const response = await verifyGuess({ item_id: item.id, guess });

      const result: RoundResult = {
        item,
        guess,
        actualPrice: response.actual_price,
        percentageOff: response.percentage_off,
        score: response.score,
      };

      const newResults = [...results, result];
      const nextRound = currentRound + 1;

      setResults(newResults);
      setGameState('revealing');

      saveProgress({
        date: getToday(),
        currentRound: nextRound,
        results: newResults,
      });
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to verify guess');
    }
  }, [gameState, currentRound, items, results]);

  const nextRound = useCallback(() => {
    const next = currentRound + 1;
    setCurrentRound(next);

    if (next >= TOTAL_ROUNDS) {
      setGameState('finished');
    } else {
      setGameState('playing');
    }
  }, [currentRound]);

  return {
    items,
    currentRound,
    results,
    gameState,
    error,
    submitGuess,
    nextRound,
    totalRounds: TOTAL_ROUNDS,
  };
}
