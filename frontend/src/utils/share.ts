import { RoundResult } from '../types';

function scoreToEmoji(score: number): string {
  if (score >= 80) return '\u{1F7E9}';
  if (score >= 50) return '\u{1F7E8}';
  return '\u{1F7E5}';
}

export function buildShareText(results: RoundResult[], streak: number): string {
  const emojiRow = results.map((r) => scoreToEmoji(r.score)).join(' ');
  return `MSRP - Daily Run\nStreak: ${streak}\n${emojiRow}`;
}

export async function copyToClipboard(text: string): Promise<boolean> {
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch {
    return false;
  }
}
