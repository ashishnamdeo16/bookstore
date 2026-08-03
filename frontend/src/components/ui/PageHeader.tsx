import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';

interface PageHeaderProps {
  eyebrow?: string;
  title: string;
  subtitle?: string;
  actions?: ReactNode;
  backTo?: string;
  backLabel?: string;
}

export function PageHeader({
  eyebrow,
  title,
  subtitle,
  actions,
  backTo,
  backLabel = 'Back',
}: PageHeaderProps) {
  return (
    <div className="md-toolbar">
      <div className="grid gap-1">
        {backTo ? (
          <Link to={backTo} className="md-back">
            ← {backLabel}
          </Link>
        ) : null}
        {eyebrow ? <p className="page-eyebrow">{eyebrow}</p> : null}
        <h1 className="md-page-title">{title}</h1>
        {subtitle ? <p className="md-page-subtitle">{subtitle}</p> : null}
      </div>
      {actions ? <div className="flex flex-wrap items-center gap-2">{actions}</div> : null}
    </div>
  );
}
