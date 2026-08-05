import type { ToastItem, ToastOptions, ToastVariant } from './types';

type Listener = (toasts: ToastItem[]) => void;

let toasts: ToastItem[] = [];
const listeners = new Set<Listener>();

function emit() {
  listeners.forEach((listener) => listener([...toasts]));
}

export function subscribe(listener: Listener): () => void {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function getToasts(): ToastItem[] {
  return [...toasts];
}

export function dismissToast(id: string) {
  toasts = toasts.filter((toast) => toast.id !== id);
  emit();
}

function createId(): string {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
    return crypto.randomUUID();
  }
  return `toast-${Date.now()}-${Math.random().toString(36).slice(2, 9)}`;
}

function pushToast(variant: ToastVariant, message: string, options: ToastOptions = {}) {
  const id = createId();
  const duration = options.duration ?? 5000;

  toasts = [
    ...toasts,
    {
      id,
      message,
      title: options.title,
      variant,
      duration,
    },
  ];
  emit();

  if (duration > 0) {
    window.setTimeout(() => dismissToast(id), duration);
  }

  return id;
}

export const toast = {
  success(message: string, options?: ToastOptions) {
    return pushToast('success', message, options);
  },
  error(message: string, options?: ToastOptions) {
    return pushToast('error', message, options);
  },
  info(message: string, options?: ToastOptions) {
    return pushToast('info', message, options);
  },
  warning(message: string, options?: ToastOptions) {
    return pushToast('warning', message, options);
  },
  dismiss: dismissToast,
};
