import Modal from './Modal';

interface HowToPlayProps {
  open: boolean;
  onClose: () => void;
}

export default function HowToPlay({ open, onClose }: HowToPlayProps) {
  return (
    <Modal open={open} onClose={onClose} title="How to Play">
      <div className="space-y-4 text-sm text-msrp-muted">
        <p className="text-msrp-text font-medium">
          Guess the sold price of 5 real eBay auction items.
        </p>

        <div className="space-y-3">
          <div className="flex gap-3">
            <span className="text-msrp-accent font-bold text-base leading-6">1.</span>
            <p>You'll see an item that actually sold on eBay, along with its title and the number of bids it received.</p>
          </div>
          <div className="flex gap-3">
            <span className="text-msrp-accent font-bold text-base leading-6">2.</span>
            <p>Type your best guess for what the item sold for.</p>
          </div>
          <div className="flex gap-3">
            <span className="text-msrp-accent font-bold text-base leading-6">3.</span>
            <p>After each guess, you'll see the actual sold price and your accuracy score out of 100.</p>
          </div>
        </div>

        <div className="border-t border-msrp-border pt-4">
          <p className="font-medium text-msrp-text mb-2">Scoring</p>
          <p className="text-xs mb-3">Scoring is based on how close you are <span className="text-msrp-text">relative</span> to the actual price — not the raw dollar difference. This keeps it fair whether an item sold for $10 or $1,000.</p>
          <div className="space-y-1.5 mb-3">
            <div className="flex items-center gap-2">
              <span className="w-3 h-3 rounded-full bg-msrp-green flex-shrink-0" />
              <span>80-100: Very close</span>
            </div>
            <div className="flex items-center gap-2">
              <span className="w-3 h-3 rounded-full bg-msrp-yellow flex-shrink-0" />
              <span>50-79: In the ballpark (within ~2×)</span>
            </div>
            <div className="flex items-center gap-2">
              <span className="w-3 h-3 rounded-full bg-msrp-red flex-shrink-0" />
              <span>0-49: Way off (more than 2× away)</span>
            </div>
          </div>
          <p className="text-xs text-msrp-muted">Example: actual price $100 → guessing $50 or $200 earns 50 pts. Guessing $400 earns 0 pts.</p>
        </div>

        <div className="border-t border-msrp-border pt-4">
          <p className="text-msrp-muted text-xs">
            A new set of 5 items is released daily at midnight. Play every day to increase your streak.
          </p>
        </div>
      </div>
    </Modal>
  );
}
