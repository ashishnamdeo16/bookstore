import type { CSSProperties, HTMLAttributes } from 'react';

type SkeletonVariant = 'text' | 'circular' | 'rectangular';

interface SkeletonProps extends HTMLAttributes<HTMLDivElement> {
  variant?: SkeletonVariant;
  width?: CSSProperties['width'];
  height?: CSSProperties['height'];
}

export function Skeleton({
  variant = 'text',
  width,
  height,
  className = '',
  style,
  ...props
}: SkeletonProps) {
  return (
    <div
      className={`skeleton skeleton--${variant} ${className}`.trim()}
      aria-hidden="true"
      style={{ width, height, ...style }}
      {...props}
    />
  );
}

const LINE_WIDTHS = ['w-1/3', 'w-full', 'w-5/6', 'w-2/3'] as const;

interface SkeletonTextProps {
  lines?: number;
  className?: string;
}

export function SkeletonText({ lines = 4, className = '' }: SkeletonTextProps) {
  return (
    <div className={`skeleton-text ${className}`.trim()} aria-hidden="true">
      {Array.from({ length: lines }, (_, index) => (
        <Skeleton key={index} className={LINE_WIDTHS[index % LINE_WIDTHS.length]} />
      ))}
    </div>
  );
}
