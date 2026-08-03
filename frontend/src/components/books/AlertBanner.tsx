interface AlertBannerProps {
  variant?: 'error' | 'success' | 'info';
  title?: string;
  children: React.ReactNode;
}

export function AlertBanner({ variant = 'info', title, children }: AlertBannerProps) {
  const styles = {
    error: 'border-red-200 bg-red-50 text-red-800',
    success: 'border-emerald-200 bg-emerald-50 text-emerald-800',
    info: 'border-slate-200 bg-slate-50 text-slate-700',
  }[variant];

  return (
    <div className={`rounded-lg border px-4 py-3 ${styles}`} role={variant === 'error' ? 'alert' : 'status'}>
      {title ? <p className="mb-1 font-semibold">{title}</p> : null}
      <div className="text-sm">{children}</div>
    </div>
  );
}
