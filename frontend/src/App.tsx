import { useGameState } from './hooks/useGameState';
import { useStats } from './hooks/useStats';
import LoadingScreen from './components/LoadingScreen';
import GamePlay from './components/GamePlay';
import RevealScreen from './components/RevealScreen';
import EndScreen from './components/EndScreen';
import { useEffect, useRef } from 'react';

export default function App() {
  const { items, currentRound, results, gameState, error, submitGuess, nextRound, totalRounds } = useGameState();
  const { stats, recordGame } = useStats();
  const hasRecorded = useRef(false);

  useEffect(() => {
    if (gameState === 'finished' && !hasRecorded.current) {
      hasRecorded.current = true;
      recordGame();
    }
  }, [gameState, recordGame]);

  if (gameState === 'loading') {
    return <LoadingScreen error={error} />;
  }

  return (
    <div className="min-h-screen bg-msrp-bg flex flex-col items-center px-4 py-6">
      <header className="mb-6">
        <h1 className="text-2xl font-bold text-white tracking-tight">MSRP</h1>
      </header>

      <main className="w-full max-w-[400px]">
        {gameState === 'playing' && items[currentRound] && (
          <GamePlay
            item={items[currentRound]}
            currentRound={currentRound}
            totalRounds={totalRounds}
            onSubmit={submitGuess}
          />
        )}

        {gameState === 'revealing' && results[results.length - 1] && (
          <RevealScreen
            result={results[results.length - 1]}
            currentRound={currentRound}
            totalRounds={totalRounds}
            onNext={nextRound}
          />
        )}

        {gameState === 'finished' && (
          <EndScreen results={results} stats={stats} />
        )}
      </main>
    </div>
  );
}
