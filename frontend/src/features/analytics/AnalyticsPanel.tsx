import type { ReactNode } from 'react';

interface AnalyticsPanelProps {
  title: string;
  subtitle?: string;
  children: ReactNode;
  className?: string;
}

export function AnalyticsPanel({ title, subtitle, children, className = '' }: AnalyticsPanelProps) {
  return (
    <section className={`analytics-panel ${className}`.trim()}>
      <header className="analytics-panel__header">
        <h2 className="analytics-panel__title">{title}</h2>
        {subtitle ? <p className="analytics-panel__subtitle">{subtitle}</p> : null}
      </header>
      <div className="analytics-panel__body">{children}</div>
    </section>
  );
}
