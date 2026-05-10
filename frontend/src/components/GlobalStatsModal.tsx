import { useEffect, useState } from 'react';
import { GlobalStats } from '../types';
import { fetchGlobalStats } from '../services/api';
import Modal from './Modal';

interface GlobalStatsModalProps {
  open: boolean;
  onClose: () => void;
  gameDate: string;
}

export default function GlobalStatsModal({ open, onClose, gameDate }: GlobalStatsModalProps) {
  const [stats, setStats] = useState<GlobalStats | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!open || !gameDate) return;
    setLoading(true);
    fetchGlobalStats(gameDate)
      .then(setStats)
      .catch(() => setStats(null))
      .finally(() => setLoading(false));
  }, [open, gameDate]);

  function fmt(val: number | null): string {
    return val !== null ? String(val) : '—';
  }

  return (
    <Modal open={open} onClose={onClose} title="Global Stats">
      <p className="text-msrp-muted text-xs mb-4">
        How everyone scored on today's puzzle ({gameDate}).
      </p>

      {loading ? (
        <p className="text-msrp-muted text-sm text-center py-4">Loading...</p>
      ) : stats && stats.playerCount > 0 ? (
        <>
          <div className="grid grid-cols-3 gap-2 mb-4">
            <div className="text-center bg-msrp-card border border-msrp-border rounded-lg p-3">
              <div className="text-2xl font-bold text-msrp-green">{fmt(stats.highScore)}</div>
              <div className="text-msrp-muted text-xs mt-1">High Score</div>
            </div>
            <div className="text-center bg-msrp-card border border-msrp-border rounded-lg p-3">
              <div className="text-2xl font-bold text-msrp-text">{fmt(stats.avgScore)}</div>
              <div className="text-msrp-muted text-xs mt-1">Average</div>
            </div>
            <div className="text-center bg-msrp-card border border-msrp-border rounded-lg p-3">
              <div className="text-2xl font-bold text-msrp-red">{fmt(stats.lowScore)}</div>
              <div className="text-msrp-muted text-xs mt-1">Low Score</div>
            </div>
          </div>
          <p className="text-msrp-muted text-xs text-center">
            Based on {stats.playerCount} {stats.playerCount === 1 ? 'player' : 'players'}
          </p>
        </>
      ) : (
        <p className="text-msrp-muted text-sm text-center py-4">
          No scores submitted yet for today.
        </p>
      )}
    </Modal>
  );
}
