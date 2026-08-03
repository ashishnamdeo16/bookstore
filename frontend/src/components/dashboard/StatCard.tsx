import { Link } from 'react-router-dom';

interface StatCardProps {
  label: string;
  value: string | number;
  hint?: string;
  to?: string;
}

export function StatCard({ label, value, hint, to }: StatCardProps) {
  const body = (
    <article className="stat-card">
      <p className="stat-card__label">{label}</p>
      <p className="stat-card__value">{value}</p>
      {hint ? <p className="stat-card__hint">{hint}</p> : null}
    </article>
  );

  if (to) {
    return (
      <Link to={to} className="stat-card-link">
        {body}
      </Link>
    );
  }

  return body;
}
