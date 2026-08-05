export { ToastProvider } from './ToastProvider';
export { toast } from './toastStore';
export type { ToastItem, ToastOptions, ToastVariant } from './types';

import { toast as toastApi } from './toastStore';

export function useToast() {
  return { toast: toastApi };
}
