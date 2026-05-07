interface ProgressBarProps {
  current: number;
  total: number;
}

export default function ProgressBar({ current, total }: ProgressBarProps) {
  return (
    <div className="w-full mb-6">
      <div className="flex justify-between text-sm text-msrp-muted mb-2">
        <span>Round {Math.min(current + 1, total)} of {total}</span>
      </div>
      <div className="w-full h-2 bg-msrp-border rounded-full overflow-hidden">
        <div
          className="h-full bg-msrp-accent rounded-full transition-all duration-500"
          style={{ width: `${(current / total) * 100}%` }}
        />
      </div>
    </div>
  );
}
