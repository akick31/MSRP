import { useEffect, useRef, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { getPuzzleNumber } from '../utils/share';
import { useGameState, loadLastResults } from '../hooks/useGameState';
import { useStats } from '../hooks/useStats';
import { useSettings } from '../hooks/useSettings';
import { useModal } from '../hooks/useModal';
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
import ProjectsModal from '../components/ProjectsModal';
import GlobalStatsModal from '../components/GlobalStatsModal';
import Footer from '../components/Footer';

const HTP_SHOWN_KEY = 'msrp-htp-shown';
const VISITOR_DATE_KEY = 'msrp-visitor-date';
const SCORE_SUBMITTED_DATE_KEY = 'msrp-score-submitted-date';

function getTodayEST(): string {
  return new Date().toLocaleDateString('en-CA', { timeZone: 'America/New_York' });
}

export default function DailyPage() {
  const navigate = useNavigate();
  const { activeModal, openModal, closeModal, switchModal } = useModal();
  const { items, currentRound, results, gameState, error, startGame, submitGuess, nextRound, totalRounds } = useGameState({ persist: true });
  const { stats, recordGame } = useStats();
  const { settings, updateSettings } = useSettings();

  const hasRecorded = useRef(false);

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
        const gameDate = items[0]?.game_date ?? '';
        if (gameDate && localStorage.getItem(SCORE_SUBMITTED_DATE_KEY) !== gameDate) {
          localStorage.setItem(SCORE_SUBMITTED_DATE_KEY, gameDate);
          submitScore(totalScore, gameDate);
        }
      }
    }
  }, [gameState, results, items, recordGame]);

  const handlePlay = useCallback(() => {
    const shown = localStorage.getItem(HTP_SHOWN_KEY);
    if (!shown && stats.gamesPlayed === 0) {
      openModal('how-to-play');
      localStorage.setItem(HTP_SHOWN_KEY, '1');
    }
    startGame();
  }, [startGame, stats.gamesPlayed, openModal]);

  const handleSelectPastGame = useCallback((date: string) => {
    navigate(`/previous_game/${date}`);
  }, [navigate]);

  const lastResults = useMemo(
    () => (gameState === 'finished' ? results : loadLastResults()),
    [gameState, results],
  );

  if (gameState === 'loading') {
    return <LoadingScreen error={error} />;
  }

  return (
    <div className="min-h-screen bg-msrp-bg flex flex-col items-center px-4 py-4">
      <Header
        onHowToPlay={() => openModal('how-to-play')}
        onPastGame={() => openModal('past-picker')}
        onStats={() => openModal('stats')}
        onGlobalStats={() => openModal('global-stats')}
        onSettings={() => openModal('settings')}
      />

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
            onPlayPastGame={() => openModal('past-picker')}
          />
        )}
      </main>

      <Footer onOpenModal={openModal} />

      <HowToPlay open={activeModal === 'how-to-play'} onClose={closeModal} onContact={() => switchModal('contact')} />
      <StatsModal open={activeModal === 'stats'} onClose={closeModal} stats={stats} lastResults={lastResults} />
      <GlobalStatsModal open={activeModal === 'global-stats'} onClose={closeModal} gameDate={items[0]?.game_date ?? ''} />
      <SettingsModal open={activeModal === 'settings'} onClose={closeModal} settings={settings} onUpdate={updateSettings} />
      <PastGamePickerModal open={activeModal === 'past-picker'} onClose={closeModal} onSelect={handleSelectPastGame} />
      <ContactModal open={activeModal === 'contact'} onClose={closeModal} />
      <ProjectsModal open={activeModal === 'projects'} onClose={closeModal} />
    </div>
  );
}
