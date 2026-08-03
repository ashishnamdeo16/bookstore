import type { ReactNode } from 'react';

interface AlertProps {
  variant?: 'error' | 'success' | 'info';
  title?: string;
  children: ReactNode;
}

export function Alert({ variant = 'info', title, children }: AlertProps) {
  return (
    <div className={`alert alert--${variant}`} role={variant === 'error' ? 'alert' : 'status'}>
      {title ? <p className="alert__title">{title}</p> : null}
      <div className="alert__body">{children}</div>
    </div>
  );
}
