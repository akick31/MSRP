import { useState, useEffect, useRef, useCallback } from 'react';
import { getPuzzleNumber } from './utils/share';
import { useGameState, loadLastResults } from './hooks/useGameState';
import { useStats } from './hooks/useStats';
import { useSettings } from './hooks/useSettings';
import LoadingScreen from './components/LoadingScreen';
import LandingPage from './components/LandingPage';
import GamePlay from './components/GamePlay';
import RevealScreen from './components/RevealScreen';
import EndScreen from './components/EndScreen';
import Header from './components/Header';
import HowToPlay from './components/HowToPlay';
import StatsModal from './components/StatsModal';
import SettingsModal from './components/SettingsModal';

const HTP_SHOWN_KEY = 'msrp-htp-shown';

export default function App() {
  const { items, currentRound, results, gameState, error, startGame, submitGuess, nextRound, totalRounds } = useGameState();
  const { stats, recordGame } = useStats();
  const { settings, updateSettings } = useSettings();
  const hasRecorded = useRef(false);

  const [htpOpen, setHtpOpen] = useState(false);
  const [statsOpen, setStatsOpen] = useState(false);
  const [settingsOpen, setSettingsOpen] = useState(false);

  useEffect(() => {
    if (gameState === 'finished' && !hasRecorded.current) {
      hasRecorded.current = true;
      recordGame();
    }
  }, [gameState, recordGame]);

  const handlePlay = useCallback(() => {
    const shown = localStorage.getItem(HTP_SHOWN_KEY);
    if (!shown && stats.gamesPlayed === 0) {
      setHtpOpen(true);
      localStorage.setItem(HTP_SHOWN_KEY, '1');
    }
    startGame();
  }, [startGame, stats.gamesPlayed]);

  const lastResults = gameState === 'finished' ? results : loadLastResults();

  if (gameState === 'loading') {
    return <LoadingScreen error={error} />;
  }

  return (
    <div className="min-h-screen bg-msrp-bg flex flex-col items-center px-4 py-4">
      {gameState !== 'landing' && (
        <Header
          onHowToPlay={() => setHtpOpen(true)}
          onStats={() => setStatsOpen(true)}
          onSettings={() => setSettingsOpen(true)}
        />
      )}

      <main className="w-full max-w-[400px] flex-1">
        {gameState === 'landing' && (
          <LandingPage puzzleNumber={getPuzzleNumber()} onPlay={handlePlay} />
        )}

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

      <footer className="mt-8 pb-4">
        <a
          href="https://buymeacoffee.com/flying_porygon"
          target="_blank"
          rel="noopener noreferrer"
          className="text-msrp-muted text-xs hover:text-msrp-accent transition-colors"
        >
          Buy me a coffee
        </a>
      </footer>

      <HowToPlay open={htpOpen} onClose={() => setHtpOpen(false)} />
      <StatsModal open={statsOpen} onClose={() => setStatsOpen(false)} stats={stats} lastResults={lastResults} />
      <SettingsModal open={settingsOpen} onClose={() => setSettingsOpen(false)} settings={settings} onUpdate={updateSettings} />
    </div>
  );
}
