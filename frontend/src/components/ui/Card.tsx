import type { ReactNode } from 'react';

interface CardProps {
  children: ReactNode;
  className?: string;
  padding?: boolean;
}

export function Card({ children, className = '', padding = true }: CardProps) {
  return (
    <div className={`md-card ${padding ? '' : '!p-0'} ${className}`.trim()}>{children}</div>
  );
}
