import { useEffect, useRef } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { recordAnalytics } from '../services/api';
import { useGameState } from '../hooks/useGameState';
import { useStats } from '../hooks/useStats';
import { useSettings } from '../hooks/useSettings';
import { useModal } from '../hooks/useModal';
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
import ProjectsModal from '../components/ProjectsModal';
import Footer from '../components/Footer';

export default function PastGamePage() {
  const { date } = useParams<{ date: string }>();
  const navigate = useNavigate();
  const { activeModal, openModal, closeModal, switchModal } = useModal();
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

  if (!date) { navigate('/'); return null; }
  if (gameState === 'loading') return <LoadingScreen error={error} />;

  return (
    <div className="min-h-screen bg-msrp-bg flex flex-col items-center px-4 py-4">
      <Header
        onHowToPlay={() => openModal('how-to-play')}
        onPastGame={() => openModal('past-picker')}
        onStats={() => openModal('stats')}
        onGlobalStats={() => openModal('global-stats')}
        onSettings={() => openModal('settings')}
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
            onPlayPastGame={() => openModal('past-picker')}
          />
        )}
      </main>

      <Footer onOpenModal={openModal} />

      <HowToPlay open={activeModal === 'how-to-play'} onClose={closeModal} onContact={() => switchModal('contact')} />
      <StatsModal open={activeModal === 'stats'} onClose={closeModal} stats={stats} lastResults={null} />
      <GlobalStatsModal open={activeModal === 'global-stats'} onClose={closeModal} gameDate={new Date().toLocaleDateString('en-CA')} />
      <SettingsModal open={activeModal === 'settings'} onClose={closeModal} settings={settings} onUpdate={updateSettings} />
      <PastGamePickerModal open={activeModal === 'past-picker'} onClose={closeModal} onSelect={d => navigate(`/previous_game/${d}`)} />
      <ContactModal open={activeModal === 'contact'} onClose={closeModal} />
      <ProjectsModal open={activeModal === 'projects'} onClose={closeModal} />
    </div>
  );
}
