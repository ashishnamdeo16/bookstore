import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { Button } from './Button';

interface EmptyStateProps {
  title: string;
  description?: string;
  actionLabel?: string;
  actionTo?: string;
  onAction?: () => void;
  icon?: ReactNode;
}

export function EmptyState({
  title,
  description,
  actionLabel,
  actionTo,
  onAction,
  icon = 'B',
}: EmptyStateProps) {
  return (
    <div className="md-empty">
      <div className="md-empty__icon" aria-hidden="true">
        {icon}
      </div>
      <h2 className="md-empty__title">{title}</h2>
      {description ? <p className="md-empty__body">{description}</p> : null}
      {actionLabel && actionTo ? (
        <Link to={actionTo} className="btn btn--primary mt-3">
          {actionLabel}
        </Link>
      ) : null}
      {actionLabel && onAction ? (
        <Button className="mt-3" onClick={onAction}>
          {actionLabel}
        </Button>
      ) : null}
    </div>
  );
}
