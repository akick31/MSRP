import { RoundResult } from '../types';

function scoreToEmoji(score: number): string {
  if (score >= 90) return '\u{1F7E9}';
  if (score >= 70) return '\u{1F7E8}';
  if (score >= 40) return '\u{1F7E7}';
  return '\u{1F7E5}';
}

export function buildShareText(results: RoundResult[], streak: number): string {
  const totalScore = results.reduce((sum, r) => sum + r.score, 0);
  const emojiRow = results.map((r) => scoreToEmoji(r.score)).join(' ');
  return `MSRP\nStreak: ${streak}\nScore: ${totalScore}/500\n${emojiRow}`;
}

export async function copyToClipboard(text: string): Promise<boolean> {
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch {
    return false;
  }
}
