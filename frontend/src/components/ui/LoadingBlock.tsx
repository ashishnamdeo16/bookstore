import { SkeletonText } from './Skeleton';

interface LoadingBlockProps {
  label?: string;
  lines?: number;
}

export function LoadingBlock({ label = 'Loading…', lines = 4 }: LoadingBlockProps) {
  return (
    <div className="loading-block" role="status" aria-live="polite" aria-label={label}>
      <span className="sr-only">{label}</span>
      <SkeletonText lines={lines} />
    </div>
  );
}
