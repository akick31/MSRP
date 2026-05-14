import { RoundResult } from '../types';

export function getPuzzleNumber(): number {
  const launch = new Date('2026-03-26');
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  launch.setHours(0, 0, 0, 0);
  return Math.floor((today.getTime() - launch.getTime()) / 86400000) + 1;
}

function scoreToEmoji(score: number): string {
  if (score >= 80) return '\u{1F7E9}';
  if (score >= 50) return '\u{1F7E8}';
  return '\u{1F7E5}';
}

export function buildShareText(results: RoundResult[], streak: number): string {
  const totalScore = results.reduce((sum, r) => sum + r.score, 0);
  const emojiRow = results.map((r) => scoreToEmoji(r.score)).join(' ');
  const puzzleNo = getPuzzleNumber();
  return `MSRP | No. ${puzzleNo}\nStreak: ${streak}\nScore: ${totalScore}/500\n${emojiRow}`;
}

export async function copyToClipboard(text: string): Promise<boolean> {
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch {
    return false;
  }
}
