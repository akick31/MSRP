import { ModalId } from '../types';

interface FooterProps {
  onOpenModal: (id: ModalId) => void;
}

export default function Footer({ onOpenModal }: FooterProps) {
  return (
    <footer className="mt-8 pb-4 text-center flex flex-col items-center gap-1.5">
      <button
        onClick={() => onOpenModal('contact')}
        className="inline-flex items-center gap-1 text-msrp-muted text-xs hover:text-msrp-accent transition-colors"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
          <polyline points="22,6 12,13 2,6" />
        </svg>
        <span>Contact Me</span>
      </button>
      <a
        href="https://ko-fi.com/andrewk26515"
        target="_blank"
        rel="noopener noreferrer"
        className="inline-flex items-center gap-1 text-msrp-muted text-xs hover:text-msrp-accent transition-colors"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <polyline points="16 18 22 12 16 6" />
          <polyline points="8 6 2 12 8 18" />
        </svg>
        <span>Support Development</span>
      </a>
      <button
        onClick={() => onOpenModal('projects')}
        className="inline-flex items-center gap-1 text-msrp-muted text-xs hover:text-msrp-accent transition-colors"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <rect x="3" y="3" width="7" height="7" />
          <rect x="14" y="3" width="7" height="7" />
          <rect x="3" y="14" width="7" height="7" />
          <rect x="14" y="14" width="7" height="7" />
        </svg>
        <span>My Other Projects</span>
      </button>
    </footer>
  );
}
