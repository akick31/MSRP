import Modal from './Modal';

interface ProjectsModalProps {
  open: boolean;
  onClose: () => void;
}

const PROJECTS = [
  {
    name: 'Savantle',
    url: 'https://savantle.com/',
    description: 'A daily baseball guessing game. Identify the MLB player from their Baseball Savant percentile rankings.',
  },
  {
    name: 'Sine',
    url: 'https://apps.apple.com/us/app/sine-the-waveform-puzzle/id6756984657',
    description: 'An oscilloscope puzzle game on iOS. Try to match your own waveform with procedurally generated ones.',
  },
];

export default function ProjectsModal({ open, onClose }: ProjectsModalProps) {
  return (
      <Modal open={open} onClose={onClose} title="More from Polyloon Studios">
        <div className="space-y-4">
          <div className="space-y-3">
            {PROJECTS.map((p) => (
                <a
                    key={p.name}
                    href={p.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex flex-col gap-1 p-3 rounded-lg border border-msrp-border bg-msrp-bg hover:border-msrp-accent transition-colors group"
                >
              <span className="text-sm font-semibold text-msrp-accent group-hover:underline">
                {p.name} ↗
              </span>
                  <span className="text-xs text-msrp-muted">{p.description}</span>
                </a>
            ))}
          </div>

          <div className="pt-2 border-t border-msrp-border text-center">
            <a
              href="https://polyloon.com"
              target="_blank"
              rel="noopener noreferrer"
              className="text-xs text-msrp-muted hover:text-msrp-accent underline transition-colors"
            >
              See everything I'm building at polyloon.com
            </a>
          </div>
        </div>
      </Modal>
  );
}