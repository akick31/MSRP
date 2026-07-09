import Modal from './Modal';

interface HowToPlayProps {
  open: boolean;
  onClose: () => void;
  onContact: () => void;
}

export default function HowToPlay({ open, onClose, onContact }: HowToPlayProps) {
  return (
    <Modal open={open} onClose={onClose} title="How to Play">
      <div className="space-y-4 text-sm text-msrp-muted max-h-[70vh] overflow-y-auto">
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
          <p className="text-xs mb-3">Scoring is based on how close you are <span className="text-msrp-text">relative</span> to the actual price.</p>
          <div className="space-y-1.5 mb-3">
            <div className="flex items-center gap-2">
              <span className="w-3 h-3 rounded-full bg-msrp-green flex-shrink-0" />
              <span>90-100: Very close</span>
            </div>
            <div className="flex items-center gap-2">
              <span className="w-3 h-3 rounded-full bg-msrp-yellow flex-shrink-0" />
              <span>70-89: Ballpark</span>
            </div>
            <div className="flex items-center gap-2">
              <span className="w-3 h-3 rounded-full bg-msrp-orange flex-shrink-0" />
              <span>40-69: Getting Cold</span>
            </div>
            <div className="flex items-center gap-2">
              <span className="w-3 h-3 rounded-full bg-msrp-red flex-shrink-0" />
              <span>0-39: Way Off</span>
            </div>
          </div>
        </div>

        <div className="border-t border-msrp-border pt-4">
          <p className="text-msrp-muted text-xs">
            A new set of 5 items is released daily at midnight. Play every day to increase your streak.
          </p>
        </div>

        <div className="border-t border-msrp-border pt-4">
          <p className="text-msrp-muted text-xs">
            MSRP is an independent project by Andrew at <a href="https://polyloon.com" target="_blank">Polyloon
            Studios</a> and is not affiliated with eBay or The Price Is Right.
          </p>
        </div>

        <div className="border-t border-msrp-border pt-3 text-xs text-msrp-muted text-center">
          <p>
            Have a question, suggestion, or bug to report?{' '}
            <button
              onClick={() => { onClose(); onContact(); }}
              className="text-msrp-accent hover:underline"
            >
              Contact me using this form.
            </button>
          </p>
        </div>
      </div>
    </Modal>
  );
}
