import { useEffect, useRef, useCallback, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getPuzzleNumber } from '../utils/share';
import { useGameState, loadLastResults } from '../hooks/useGameState';
import { useStats } from '../hooks/useStats';
import { useSettings } from '../hooks/useSettings';
import { recordAnalytics, submitScore } from '../services/api';
import LoadingScreen from '../components/LoadingScreen';
import LandingPage from '../components/LandingPage';
import GamePlay from '../components/GamePlay';
import RevealScreen from '../components/RevealScreen';
import EndScreen from '../components/EndScreen';
import Header from '../components/Header';
import HowToPlay from '../components/HowToPlay';
import StatsModal from '../components/StatsModal';
import SettingsModal from '../components/SettingsModal';
import PastGamePickerModal from '../components/PastGamePickerModal';
import ContactModal from '../components/ContactModal';
import GlobalStatsModal from '../components/GlobalStatsModal';

const HTP_SHOWN_KEY = 'msrp-htp-shown';
const VISITOR_DATE_KEY = 'msrp-visitor-date';

function getTodayEST(): string {
  return new Date().toLocaleDateString('en-CA', { timeZone: 'America/New_York' });
}

export default function DailyPage() {
  const navigate = useNavigate();
  const { items, currentRound, results, gameState, error, startGame, submitGuess, nextRound, totalRounds } = useGameState({ persist: true });
  const { stats, recordGame } = useStats();
  const { settings, updateSettings } = useSettings();

  const hasRecorded = useRef(false);
  const scoreSubmitted = useRef(false);

  const [htpOpen, setHtpOpen] = useState(false);
  const [statsOpen, setStatsOpen] = useState(false);
  const [globalStatsOpen, setGlobalStatsOpen] = useState(false);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [pastPickerOpen, setPastPickerOpen] = useState(false);
  const [contactOpen, setContactOpen] = useState(false);

  useEffect(() => {
    const today = getTodayEST();
    const lastRecorded = localStorage.getItem(VISITOR_DATE_KEY);
    if (lastRecorded !== today) {
      localStorage.setItem(VISITOR_DATE_KEY, today);
      recordAnalytics('UNIQUE_VISITORS');
    }
  }, []);

  useEffect(() => {
    if (gameState === 'finished') {
      if (!hasRecorded.current) {
        hasRecorded.current = true;
        const totalScore = results.reduce((sum, r) => sum + r.score, 0);
        recordGame(totalScore);
        recordAnalytics('GAMES_PLAYED');
        if (!scoreSubmitted.current) {
          scoreSubmitted.current = true;
          const gameDate = items[0]?.game_date ?? '';
          if (gameDate) submitScore(totalScore, gameDate);
        }
      }
    }
  }, [gameState, results, items, recordGame]);

  const handlePlay = useCallback(() => {
    const shown = localStorage.getItem(HTP_SHOWN_KEY);
    if (!shown && stats.gamesPlayed === 0) {
      setHtpOpen(true);
      localStorage.setItem(HTP_SHOWN_KEY, '1');
    }
    startGame();
  }, [startGame, stats.gamesPlayed]);

  const handleSelectPastGame = useCallback((date: string) => {
    navigate(`/previous_game/${date}`);
  }, [navigate]);

  const lastResults = gameState === 'finished' ? results : loadLastResults();

  if (gameState === 'loading') {
    return <LoadingScreen error={error} />;
  }

  return (
    <div className="min-h-screen bg-msrp-bg flex flex-col items-center px-4 py-4">
      {gameState !== 'landing' && (
        <Header
          onHowToPlay={() => setHtpOpen(true)}
          onPastGame={() => setPastPickerOpen(true)}
          onStats={() => setStatsOpen(true)}
          onGlobalStats={() => setGlobalStatsOpen(true)}
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
          <EndScreen
            results={results}
            stats={stats}
            gameDate={items[0]?.game_date ?? ''}
            onPlayPastGame={() => setPastPickerOpen(true)}
          />
        )}
      </main>

      <footer className="mt-8 pb-4 text-center flex flex-col items-center gap-1.5">
        <button
          onClick={() => setContactOpen(true)}
          className="inline-flex items-center gap-1 text-msrp-muted text-xs hover:text-msrp-accent transition-colors"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
            <polyline points="22,6 12,13 2,6" />
          </svg>
          <span>Contact Me</span>
        </button>
        <a
          href="https://buymeacoffee.com/flying_porygon"
          target="_blank"
          rel="noopener noreferrer"
          className="inline-flex items-center gap-1 text-msrp-muted text-xs hover:text-msrp-accent transition-colors"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M18 8h1a4 4 0 0 1 0 8h-1" />
            <path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z" />
            <line x1="6" y1="1" x2="6" y2="4" />
            <line x1="10" y1="1" x2="10" y2="4" />
            <line x1="14" y1="1" x2="14" y2="4" />
          </svg>
          Buy me a coffee
        </a>
      </footer>

      <HowToPlay open={htpOpen} onClose={() => setHtpOpen(false)} onContact={() => setContactOpen(true)} />
      <StatsModal open={statsOpen} onClose={() => setStatsOpen(false)} stats={stats} lastResults={lastResults} />
      <GlobalStatsModal open={globalStatsOpen} onClose={() => setGlobalStatsOpen(false)} gameDate={items[0]?.game_date ?? ''} />
      <SettingsModal open={settingsOpen} onClose={() => setSettingsOpen(false)} settings={settings} onUpdate={updateSettings} />
      <PastGamePickerModal open={pastPickerOpen} onClose={() => setPastPickerOpen(false)} onSelect={handleSelectPastGame} />
      <ContactModal open={contactOpen} onClose={() => setContactOpen(false)} />
    </div>
  );
}
