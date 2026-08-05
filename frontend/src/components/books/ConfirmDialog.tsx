import type { ReactNode } from 'react';
import { Button } from '../ui/Button';

interface ConfirmDialogProps {
  open: boolean;
  title: string;
  message: string;
  confirmLabel?: string;
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function ConfirmDialog({
  open,
  title,
  message,
  confirmLabel = 'Delete',
  loading = false,
  onConfirm,
  onCancel,
}: ConfirmDialogProps) {
  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-[var(--md-backdrop)] p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="confirm-dialog-title"
    >
      <div className="w-full max-w-md rounded-xl border border-[var(--md-outline)] bg-[var(--md-surface)] p-6 shadow-[var(--md-elev-3)]">
        <h2 id="confirm-dialog-title" className="text-[22px] font-normal text-[var(--md-on-surface)]">
          {title}
        </h2>
        <p className="mt-3 text-sm leading-relaxed text-[var(--md-on-surface-variant)]">{message}</p>
        <div className="mt-6 flex justify-end gap-2">
          <Button type="button" variant="ghost" onClick={onCancel} disabled={loading}>
            Cancel
          </Button>
          <Button type="button" variant="danger" onClick={onConfirm} loading={loading}>
            {confirmLabel}
          </Button>
        </div>
      </div>
    </div>
  );
}

export { LoadingBlock } from '../ui/LoadingBlock';

export function SectionCard({
  children,
  className = '',
}: {
  children: ReactNode;
  className?: string;
}) {
  return <section className={`md-card ${className}`.trim()}>{children}</section>;
}
