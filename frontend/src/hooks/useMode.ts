import { useState } from 'react';

export type GameMode = 'easy' | 'hard';
const MODE_KEY = 'msrp-mode';

export function useMode() {
  const [mode, setModeState] = useState<GameMode>(() => {
    return (localStorage.getItem(MODE_KEY) as GameMode) ?? 'hard';
  });

  function setMode(m: GameMode) {
    localStorage.setItem(MODE_KEY, m);
    setModeState(m);
  }

  return { mode, setMode };
}
