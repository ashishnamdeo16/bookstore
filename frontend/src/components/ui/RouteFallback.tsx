import { Spinner } from '../ui/Spinner';

export function RouteFallback({ label = 'Loading page' }: { label?: string }) {
  return (
    <div className="route-fallback" role="status" aria-live="polite" aria-label={label}>
      <Spinner label={label} />
      <p className="route-fallback__text">{label}…</p>
    </div>
  );
}
