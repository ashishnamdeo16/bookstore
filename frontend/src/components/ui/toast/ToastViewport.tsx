import type { ToastItem } from './types';
import { dismissToast } from './toastStore';

const ICONS: Record<ToastItem['variant'], string> = {
  success: '✓',
  error: '!',
  info: 'i',
  warning: '⚠',
};

interface ToastViewportProps {
  toasts: ToastItem[];
}

export function ToastViewport({ toasts }: ToastViewportProps) {
  if (toasts.length === 0) return null;

  return (
    <div className="toast-viewport" aria-live="polite" aria-relevant="additions text">
      {toasts.map((item) => (
        <div
          key={item.id}
          className={`toast toast--${item.variant} animate-fade-in`}
          role={item.variant === 'error' ? 'alert' : 'status'}
        >
          <span className="toast__icon" aria-hidden="true">
            {ICONS[item.variant]}
          </span>
          <div className="toast__content">
            {item.title ? <p className="toast__title">{item.title}</p> : null}
            <p className="toast__message">{item.message}</p>
          </div>
          <button
            type="button"
            className="toast__close"
            onClick={() => dismissToast(item.id)}
            aria-label="Dismiss notification"
          >
            ×
          </button>
        </div>
      ))}
    </div>
  );
}
