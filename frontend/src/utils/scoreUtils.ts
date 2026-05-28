function scoreTheme(score: number) {
  if (score >= 90) return { text: 'text-msrp-green', bg: 'bg-msrp-green/20', dot: 'bg-msrp-green' } as const;
  if (score >= 70) return { text: 'text-msrp-yellow', bg: 'bg-msrp-yellow/20', dot: 'bg-msrp-yellow' } as const;
  if (score >= 40) return { text: 'text-msrp-orange', bg: 'bg-msrp-orange/20', dot: 'bg-msrp-orange' } as const;
  return { text: 'text-msrp-red', bg: 'bg-msrp-red/20', dot: 'bg-msrp-red' } as const;
}

export function getScoreColor(score: number): string { return scoreTheme(score).text; }
export function getScoreBg(score: number): string { return scoreTheme(score).bg; }
export function getScoreDot(score: number): string { return scoreTheme(score).dot; }
