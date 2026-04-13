import { useState } from 'react';
import { DailyItem } from '../types';
import ProgressBar from './ProgressBar';

interface GamePlayProps {
  item: DailyItem;
  currentRound: number;
  totalRounds: number;
  onSubmit: (guess: number) => void;
}

export default function GamePlay({ item, currentRound, totalRounds, onSubmit }: GamePlayProps) {
  const [input, setInput] = useState('');
  const [submitting, setSubmitting] = useState(false);

  function handleInputChange(value: string) {
    const cleaned = value.replace(/[^0-9.]/g, '');
    const parts = cleaned.split('.');
    if (parts.length > 2) return;
    if (parts[1] && parts[1].length > 2) return;
    setInput(cleaned);
  }

  async function handleSubmit() {
    const guess = parseFloat(input);
    if (isNaN(guess) || guess <= 0) return;

    setSubmitting(true);
    await onSubmit(guess);
    setSubmitting(false);
    setInput('');
  }

  function handleKeyDown(e: React.KeyboardEvent) {
    if (e.key === 'Enter') {
      handleSubmit();
    }
  }

  const guessValue = parseFloat(input);
  const isValid = !isNaN(guessValue) && guessValue > 0;

  return (
    <div className="w-full">
      <ProgressBar current={currentRound} total={totalRounds} />

      <div className="bg-msrp-card rounded-xl overflow-hidden border border-msrp-border">
        <div className="w-full aspect-square bg-black flex items-center justify-center overflow-hidden">
          <img
            src={item.image_url}
            alt={item.title}
            className="w-full h-full object-contain"
            loading="eager"
          />
        </div>

        <div className="p-4">
          <h2 className="text-msrp-text font-semibold text-base leading-tight mb-3">
            {item.title}
          </h2>

          <div className="inline-block bg-msrp-border text-msrp-muted text-xs font-medium px-2.5 py-1 rounded-md mb-4">
            {item.bid_count} bids
          </div>

          <div className="flex gap-2">
            <div className="relative flex-1">
              <span className="absolute left-3 top-1/2 -translate-y-1/2 text-msrp-muted text-lg">$</span>
              <input
                type="tel"
                inputMode="decimal"
                value={input}
                onChange={(e) => handleInputChange(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="0.00"
                disabled={submitting}
                className="w-full bg-msrp-bg border border-msrp-border rounded-lg pl-8 pr-3 py-3 text-msrp-text text-lg font-medium placeholder-msrp-border focus:outline-none focus:border-msrp-accent transition-colors"
              />
            </div>

            <button
              onClick={handleSubmit}
              disabled={!isValid || submitting}
              className="px-5 py-3 bg-msrp-accent text-msrp-bg font-bold rounded-lg disabled:opacity-40 disabled:cursor-not-allowed hover:brightness-110 active:brightness-90 transition-all whitespace-nowrap"
            >
              {submitting ? '...' : 'Guess'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
