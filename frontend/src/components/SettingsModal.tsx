import { Settings } from '../hooks/useSettings';
import Modal from './Modal';

interface SettingsModalProps {
  open: boolean;
  onClose: () => void;
  settings: Settings;
  onUpdate: (partial: Partial<Settings>) => void;
}

function Toggle({ enabled, onToggle }: { enabled: boolean; onToggle: () => void }) {
  return (
    <button
      onClick={onToggle}
      className={`relative w-11 h-6 rounded-full transition-colors ${
        enabled ? 'bg-msrp-accent' : 'bg-msrp-border'
      }`}
      aria-checked={enabled}
      role="switch"
    >
      <span
        className={`absolute top-0.5 left-0.5 w-5 h-5 bg-white rounded-full transition-transform shadow-sm ${
          enabled ? 'translate-x-5' : 'translate-x-0'
        }`}
      />
    </button>
  );
}

export default function SettingsModal({ open, onClose, settings, onUpdate }: SettingsModalProps) {
  return (
    <Modal open={open} onClose={onClose} title="Settings">
      <div className="space-y-4">
        <div className="flex items-center justify-between py-2">
          <div>
            <div className="text-msrp-text font-medium text-sm">Dark Mode</div>
            <div className="text-msrp-muted text-xs">Use dark background</div>
          </div>
          <Toggle
            enabled={settings.darkMode}
            onToggle={() => onUpdate({ darkMode: !settings.darkMode })}
          />
        </div>

        <div className="border-t border-msrp-border" />

        <div className="flex items-center justify-between py-2">
          <div>
            <div className="text-msrp-text font-medium text-sm">High Contrast</div>
            <div className="text-msrp-muted text-xs">Increase color contrast</div>
          </div>
          <Toggle
            enabled={settings.highContrast}
            onToggle={() => onUpdate({ highContrast: !settings.highContrast })}
          />
        </div>
      </div>
    </Modal>
  );
}
