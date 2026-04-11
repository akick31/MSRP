import { useState } from 'react';
import { PlayerStats, RoundResult } from '../types';
import { buildShareText, copyToClipboard } from '../utils/share';
import Modal from './Modal';

interface StatsModalProps {
  open: boolean;
  onClose: () => void;
  stats: PlayerStats;
  lastResults: RoundResult[] | null;
}

export default function StatsModal({ open, onClose, stats, lastResults }: StatsModalProps) {
  const [copied, setCopied] = useState(false);

  async function handleShare() {
    if (!lastResults || lastResults.length === 0) return;
    const text = buildShareText(lastResults, stats.currentStreak);
    const success = await copyToClipboard(text);
    if (success) {
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  }

  return (
    <Modal open={open} onClose={onClose} title="Statistics">
      <div className="grid grid-cols-4 gap-2 mb-6">
        <div className="text-center">
          <div className="text-2xl font-bold text-msrp-text">{stats.gamesPlayed}</div>
          <div className="text-msrp-muted text-xs mt-1">Played</div>
        </div>
        <div className="text-center">
          <div className="text-2xl font-bold text-msrp-text">{stats.currentStreak}</div>
          <div className="text-msrp-muted text-xs mt-1">Current Streak</div>
        </div>
        <div className="text-center">
          <div className="text-2xl font-bold text-msrp-text">{stats.maxStreak}</div>
          <div className="text-msrp-muted text-xs mt-1">Max Streak</div>
        </div>
        <div className="text-center">
          <div className="text-2xl font-bold text-msrp-text">
            {lastResults && lastResults.length > 0
              ? lastResults.reduce((sum, r) => sum + r.score, 0)
              : '-'}
          </div>
          <div className="text-msrp-muted text-xs mt-1">Last Score</div>
        </div>
      </div>

      {lastResults && lastResults.length > 0 && (
        <button
          onClick={handleShare}
          className="w-full py-3 bg-msrp-accent text-msrp-bg font-bold rounded-lg hover:brightness-110 active:brightness-90 transition-all"
        >
          {copied ? 'Copied!' : 'Share Last Score'}
        </button>
      )}
    </Modal>
  );
}
