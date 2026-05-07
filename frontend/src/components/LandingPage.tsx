interface LandingPageProps {
  puzzleNumber: number;
  onPlay: () => void;
  hidePuzzleNumber?: boolean;
}

function formatDate(): string {
  return new Date().toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}

export default function LandingPage({ puzzleNumber, onPlay, hidePuzzleNumber = false }: LandingPageProps) {
  return (
    <div className="flex flex-col items-center justify-center min-h-[70vh] text-center">
      <div className="text-5xl font-bold text-msrp-accent mb-2 tracking-tight">$</div>
      <h1 className="text-4xl font-bold text-msrp-text tracking-tight mb-3">MSRP</h1>
      <p className="text-msrp-muted text-base mb-6 max-w-[280px]">
        Guess the price that 5 real eBay auction items sold for.
      </p>

      {!hidePuzzleNumber && (
        <div className="text-msrp-muted text-sm mb-8">
          <div>{formatDate()}</div>
          <div className="text-xs mt-1">No. {puzzleNumber}</div>
        </div>
      )}

      <button
        onClick={onPlay}
        className="px-10 py-3 bg-msrp-accent text-msrp-bg font-bold text-lg rounded-lg hover:brightness-110 active:brightness-90 transition-all"
      >
        Play
      </button>
    </div>
  );
}
