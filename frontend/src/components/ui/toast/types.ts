export type ToastVariant = 'success' | 'error' | 'info' | 'warning';

export interface ToastItem {
  id: string;
  message: string;
  title?: string;
  variant: ToastVariant;
  duration: number;
}

export interface ToastOptions {
  title?: string;
  duration?: number;
}
