import { Link } from 'react-router-dom';

interface HeaderProps {
  onHowToPlay: () => void;
  onPastGame: () => void;
  onStats: () => void;
  onGlobalStats: () => void;
  onSettings: () => void;
}

function Divider() {
  return <div className="w-px h-5 bg-msrp-border" />;
}

export default function Header({ onHowToPlay, onPastGame, onStats, onGlobalStats, onSettings }: HeaderProps) {
  return (
    <header className="w-full max-w-[400px] flex flex-col items-center mb-4">
      <Link to="/" className="font-brand text-4xl text-msrp-text mb-2">MSRP</Link>

      <nav className="w-full flex items-center justify-center gap-3">
        <button
          onClick={onHowToPlay}
          className="p-1 text-msrp-muted hover:text-msrp-text transition-colors"
          aria-label="How to play"
          title="How to play"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="12" cy="12" r="10" />
            <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3" />
            <line x1="12" y1="17" x2="12.01" y2="17" />
          </svg>
        </button>

        <Divider />

        <Link to="/" className="text-sm font-semibold text-msrp-muted hover:text-msrp-text transition-colors">
          Today
        </Link>
        <button
          onClick={onPastGame}
          className="text-sm font-semibold text-msrp-muted hover:text-msrp-text transition-colors"
        >
          Replay
        </button>

        <Divider />

        <button
          onClick={onGlobalStats}
          className="p-1 text-msrp-muted hover:text-msrp-text transition-colors"
          aria-label="Global statistics"
          title="Global statistics"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="12" cy="12" r="10" />
            <line x1="2" y1="12" x2="22" y2="12" />
            <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
          </svg>
        </button>
        <button
          onClick={onStats}
          className="p-1 text-msrp-muted hover:text-msrp-text transition-colors"
          aria-label="Statistics"
          title="Statistics"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="12" width="4" height="9" rx="1" />
            <rect x="10" y="7" width="4" height="14" rx="1" />
            <rect x="17" y="3" width="4" height="18" rx="1" />
          </svg>
        </button>
        <button
          onClick={onSettings}
          className="p-1 text-msrp-muted hover:text-msrp-text transition-colors"
          aria-label="Settings"
          title="Settings"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z" />
            <circle cx="12" cy="12" r="3" />
          </svg>
        </button>
      </nav>
    </header>
  );
}
