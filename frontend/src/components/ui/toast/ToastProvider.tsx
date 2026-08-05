import { useEffect, useState, type ReactNode } from 'react';
import { ToastViewport } from './ToastViewport';
import { getToasts, subscribe } from './toastStore';
import type { ToastItem } from './types';

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<ToastItem[]>(() => getToasts());

  useEffect(() => subscribe(setToasts), []);

  return (
    <>
      {children}
      <ToastViewport toasts={toasts} />
    </>
  );
}
