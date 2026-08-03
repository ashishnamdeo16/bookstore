import type { ReactNode } from 'react';

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
      className="fixed inset-0 z-50 flex items-center justify-center bg-[rgba(32,33,36,0.48)] p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="confirm-dialog-title"
    >
      <div className="w-full max-w-md rounded-xl border border-[var(--md-outline)] bg-white p-6 shadow-[var(--md-elev-3)]">
        <h2 id="confirm-dialog-title" className="text-[22px] font-normal text-[var(--md-on-surface)]">
          {title}
        </h2>
        <p className="mt-3 text-sm leading-relaxed text-[var(--md-on-surface-variant)]">{message}</p>
        <div className="mt-6 flex justify-end gap-2">
          <button
            type="button"
            className="btn btn--ghost"
            onClick={onCancel}
            disabled={loading}
          >
            Cancel
          </button>
          <button
            type="button"
            className="btn btn--danger"
            onClick={onConfirm}
            disabled={loading}
          >
            {loading ? 'Deleting…' : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}

export function LoadingBlock({ label = 'Loading…' }: { label?: string }) {
  return (
    <div className="md-loading-card" role="status" aria-live="polite" aria-label={label}>
      <div className="md-skeleton w-1/3" />
      <div className="md-skeleton w-full" />
      <div className="md-skeleton w-5/6" />
      <div className="md-skeleton w-2/3" />
    </div>
  );
}

export function SectionCard({
  children,
  className = '',
}: {
  children: ReactNode;
  className?: string;
}) {
  return <section className={`md-card ${className}`.trim()}>{children}</section>;
}
