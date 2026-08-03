interface SpinnerProps {
  label?: string;
  size?: 'sm' | 'md';
}

export function Spinner({ label = 'Loading', size = 'md' }: SpinnerProps) {
  return (
    <div className={`spinner spinner--${size}`} role="status" aria-label={label}>
      <span className="spinner__ring" aria-hidden="true" />
      <span className="visually-hidden">{label}</span>
    </div>
  );
}
