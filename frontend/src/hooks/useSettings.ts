import { useState, useCallback, useEffect } from 'react';

export interface Settings {
  darkMode: boolean;
  highContrast: boolean;
}

const SETTINGS_KEY = 'msrp-settings';

function loadSettings(): Settings {
  try {
    const raw = localStorage.getItem(SETTINGS_KEY);
    if (raw) return JSON.parse(raw);
  } catch {
  }
  return { darkMode: true, highContrast: false };
}

function applyTheme(settings: Settings): void {
  const root = document.documentElement;
  root.classList.toggle('light', !settings.darkMode);
  root.classList.toggle('high-contrast', settings.highContrast);
}

export function useSettings() {
  const [settings, setSettings] = useState<Settings>(loadSettings);

  useEffect(() => {
    applyTheme(settings);
  }, [settings]);

  const updateSettings = useCallback((partial: Partial<Settings>) => {
    setSettings((prev) => {
      const next = { ...prev, ...partial };
      localStorage.setItem(SETTINGS_KEY, JSON.stringify(next));
      return next;
    });
  }, []);

  return { settings, updateSettings };
}
