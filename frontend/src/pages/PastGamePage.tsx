import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { recordAnalytics } from '../services/api';
import { useGameState } from '../hooks/useGameState';
import { useStats } from '../hooks/useStats';
import { useSettings } from '../hooks/useSettings';
import LoadingScreen from '../components/LoadingScreen';
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

export default function PastGamePage() {
  const { date } = useParams<{ date: string }>();
  const navigate = useNavigate();
  const { items, currentRound, results, gameState, error, startGame, submitGuess, nextRound, totalRounds } =
    useGameState({ overrideDate: date, persist: false });
  const { stats } = useStats();
  const { settings, updateSettings } = useSettings();

  const replayRecorded = useRef(false);

  useEffect(() => {
    if (gameState === 'landing') startGame();
  }, [gameState, startGame]);

  useEffect(() => {
    if (gameState === 'finished' && !replayRecorded.current) {
      replayRecorded.current = true;
      recordAnalytics('REPLAY_PLAYED');
    }
  }, [gameState]);

  const [htpOpen, setHtpOpen] = useState(false);
  const [statsOpen, setStatsOpen] = useState(false);
  const [globalStatsOpen, setGlobalStatsOpen] = useState(false);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [pastPickerOpen, setPastPickerOpen] = useState(false);
  const [contactOpen, setContactOpen] = useState(false);

  if (!date) { navigate('/'); return null; }
  if (gameState === 'loading') return <LoadingScreen error={error} />;

  return (
    <div className="min-h-screen bg-msrp-bg flex flex-col items-center px-4 py-4">
      <Header
        onHowToPlay={() => setHtpOpen(true)}
        onPastGame={() => setPastPickerOpen(true)}
        onStats={() => setStatsOpen(true)}
        onGlobalStats={() => setGlobalStatsOpen(true)}
        onSettings={() => setSettingsOpen(true)}
      />

      <div className="w-full max-w-[400px] mb-3 flex items-center justify-between">
        <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-msrp-card border border-msrp-border text-xs text-msrp-muted">
          Past Game · {date}
        </div>
        <Link to="/" className="text-xs text-msrp-muted hover:text-msrp-text transition-colors">
          Back to today
        </Link>
      </div>

      <main className="w-full max-w-[400px] flex-1">
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
            gameDate={items[0]?.game_date ?? date ?? ''}
            isPastGame
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
      <StatsModal open={statsOpen} onClose={() => setStatsOpen(false)} stats={stats} lastResults={null} />
      <GlobalStatsModal open={globalStatsOpen} onClose={() => setGlobalStatsOpen(false)} gameDate={items[0]?.game_date ?? date ?? ''} />
      <SettingsModal open={settingsOpen} onClose={() => setSettingsOpen(false)} settings={settings} onUpdate={updateSettings} />
      <PastGamePickerModal
        open={pastPickerOpen}
        onClose={() => setPastPickerOpen(false)}
        onSelect={d => navigate(`/previous_game/${d}`)}
      />
      <ContactModal open={contactOpen} onClose={() => setContactOpen(false)} />
    </div>
  );
}
