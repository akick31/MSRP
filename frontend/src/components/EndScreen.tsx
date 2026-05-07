import { useState } from 'react';
import { RoundResult, PlayerStats } from '../types';
import { buildShareText, copyToClipboard } from '../utils/share';

interface EndScreenProps {
  results: RoundResult[];
  stats: PlayerStats;
  isPastGame?: boolean;
  onPlayPastGame?: () => void;
}

function formatPrice(price: number): string {
  return price.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}

function getScoreColor(score: number): string {
  if (score >= 90) return 'text-msrp-green';
  if (score >= 70) return 'text-msrp-yellow';
  if (score >= 40) return 'text-msrp-orange';
  return 'text-msrp-red';
}

function getScoreDot(score: number): string {
  if (score >= 90) return 'bg-msrp-green';
  if (score >= 70) return 'bg-msrp-yellow';
  if (score >= 40) return 'bg-msrp-orange';
  return 'bg-msrp-red';
}

export default function EndScreen({ results, stats, isPastGame = false, onPlayPastGame }: EndScreenProps) {
  const [copied, setCopied] = useState(false);

  const totalScore = results.reduce((sum, r) => sum + r.score, 0);

  async function handleShare() {
    const text = buildShareText(results, stats.currentStreak);
    const success = await copyToClipboard(text);
    if (success) {
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  }

  return (
    <div className="w-full">
      <div className="text-center mb-6">
        <h2 className="text-2xl font-bold text-msrp-text mb-1">Game Complete</h2>
        <p className="text-msrp-muted">Total Score: <span className="text-msrp-text font-bold">{totalScore}/500</span></p>
      </div>

      {!isPastGame && (
        <div className="grid grid-cols-2 gap-3 mb-6">
          <div className="bg-msrp-card rounded-lg p-4 border border-msrp-border text-center">
            <div className="text-2xl font-bold text-msrp-text">{stats.currentStreak}</div>
            <div className="text-msrp-muted text-xs mt-1">Current Streak</div>
          </div>
          <div className="bg-msrp-card rounded-lg p-4 border border-msrp-border text-center">
            <div className="text-2xl font-bold text-msrp-text">{stats.maxStreak}</div>
            <div className="text-msrp-muted text-xs mt-1">Max Streak</div>
          </div>
        </div>
      )}

      <div className="space-y-2 mb-6">
        {results.map((result, i) => (
          <div key={i} className="flex gap-3 bg-msrp-card rounded-lg p-3 border border-msrp-border">
            <div className={`w-2.5 h-2.5 rounded-full flex-shrink-0 mt-1.5 ${getScoreDot(result.score)}`} />
            <div className="flex-1 min-w-0">
              <div className="flex items-start justify-between gap-2">
                <p className="text-msrp-text text-sm font-medium w-full min-w-0 break-words [overflow-wrap:anywhere]">
                  {result.item.title}
                </p>
                <div className={`font-bold text-sm flex-shrink-0 tabular-nums ${getScoreColor(result.score)}`}>
                  {result.score}
                </div>
              </div>
              <div className="text-msrp-muted text-xs mt-1">
                Guess: {formatPrice(result.guess)} | Sold: {formatPrice(result.actualPrice)}
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="space-y-2">
        {!isPastGame && (
          <button
            onClick={handleShare}
            className="w-full py-3 bg-msrp-accent text-msrp-bg font-bold rounded-lg hover:brightness-110 active:brightness-90 transition-all"
          >
            {copied ? 'Copied!' : 'Share Results'}
          </button>
        )}
        <button
          onClick={onPlayPastGame}
          className="w-full py-3 border border-msrp-border text-msrp-muted font-semibold rounded-lg hover:text-msrp-text hover:border-msrp-accent transition-colors"
        >
          Play a Previous Day
        </button>
      </div>
    </div>
  );
}
