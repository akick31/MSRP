export function getScoreColor(score: number): string {
  if (score >= 90) return 'text-msrp-green';
  if (score >= 70) return 'text-msrp-yellow';
  if (score >= 40) return 'text-msrp-orange';
  return 'text-msrp-red';
}

export function getScoreBg(score: number): string {
  if (score >= 90) return 'bg-msrp-green/20';
  if (score >= 70) return 'bg-msrp-yellow/20';
  if (score >= 40) return 'bg-msrp-orange/20';
  return 'bg-msrp-red/20';
}

export function getScoreDot(score: number): string {
  if (score >= 90) return 'bg-msrp-green';
  if (score >= 70) return 'bg-msrp-yellow';
  if (score >= 40) return 'bg-msrp-orange';
  return 'bg-msrp-red';
}
