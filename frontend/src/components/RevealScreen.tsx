import { RoundResult } from '../types';
import { ebayHighResImageUrl } from '../utils/ebayImage';
import ProgressBar from './ProgressBar';

interface RevealScreenProps {
  result: RoundResult;
  currentRound: number;
  totalRounds: number;
  onNext: () => void;
}

function getScoreColor(score: number): string {
  if (score >= 90) return 'text-msrp-green';
  if (score >= 70) return 'text-msrp-yellow';
  if (score >= 40) return 'text-msrp-orange';
  return 'text-msrp-red';
}

function getScoreBg(score: number): string {
  if (score >= 90) return 'bg-msrp-green/10 border-msrp-green/30';
  if (score >= 70) return 'bg-msrp-yellow/10 border-msrp-yellow/30';
  if (score >= 40) return 'bg-msrp-orange/10 border-msrp-orange/30';
  return 'bg-msrp-red/10 border-msrp-red/30';
}

function formatPrice(price: number): string {
  return price.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}

export default function RevealScreen({ result, currentRound, totalRounds, onNext }: RevealScreenProps) {
  const isLastRound = currentRound + 1 >= totalRounds;

  return (
    <div className="w-full">
      <ProgressBar current={currentRound + 1} total={totalRounds} />

      <div className="bg-msrp-card rounded-xl overflow-hidden border border-msrp-border">
        <div className="w-full aspect-video bg-black flex items-center justify-center overflow-hidden">
          <img
            src={ebayHighResImageUrl(result.item.image_url)}
            alt={result.item.title}
            className="w-full h-full object-contain"
            decoding="async"
          />
        </div>

        <div className="p-4">
          <h2 className="text-msrp-text font-semibold text-base leading-snug mb-4 w-full break-words [overflow-wrap:anywhere]">
            {result.item.title}
          </h2>

          <div className="grid grid-cols-2 gap-3 mb-4">
            <div className="bg-msrp-bg rounded-lg p-3 border border-msrp-border">
              <div className="text-msrp-muted text-xs mb-1">Your Guess</div>
              <div className="text-msrp-text font-bold text-lg">{formatPrice(result.guess)}</div>
            </div>
            <div className="bg-msrp-bg rounded-lg p-3 border border-msrp-border">
              <div className="text-msrp-muted text-xs mb-1">Sold Price</div>
              <div className="text-msrp-accent font-bold text-lg">{formatPrice(result.actualPrice)}</div>
            </div>
          </div>

          <div className={`rounded-lg p-3 border mb-4 ${getScoreBg(result.score)}`}>
            <div className="flex items-center justify-between">
              <div>
                <div className="text-msrp-muted text-xs mb-1">Accuracy</div>
                <div className={`font-bold text-xl ${getScoreColor(result.score)}`}>
                  {result.score}/100
                </div>
              </div>
              <div className="text-right">
                <div className="text-msrp-muted text-xs mb-1">Off By</div>
                <div className={`font-semibold ${getScoreColor(result.score)}`}>
                  {result.percentageOff}%
                </div>
              </div>
            </div>
          </div>

          <div className="flex flex-col gap-2">
            {result.item.item_url && (
              <a
                href={result.item.item_url}
                target="_blank"
                rel="noopener noreferrer"
                className="block w-full py-2.5 text-center text-msrp-muted text-sm font-medium border border-msrp-border rounded-lg hover:text-msrp-text hover:border-msrp-accent transition-colors"
              >
                View on eBay
              </a>
            )}
            <button
              onClick={onNext}
              className="w-full py-3 bg-msrp-accent text-msrp-bg font-bold rounded-lg hover:brightness-110 active:brightness-90 transition-all"
            >
              {isLastRound ? 'See Results' : 'Next Item'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
