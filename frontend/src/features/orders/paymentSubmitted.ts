const PREFIX = 'bookstore:paymentSubmitted:';

/** Mark that Stripe already confirmed payment for this order (webhook may still be in flight). */
export function markPaymentSubmitted(orderId: string): void {
  sessionStorage.setItem(`${PREFIX}${orderId}`, String(Date.now()));
}

export function clearPaymentSubmitted(orderId: string): void {
  sessionStorage.removeItem(`${PREFIX}${orderId}`);
}

export function wasPaymentSubmitted(orderId: string): boolean {
  return sessionStorage.getItem(`${PREFIX}${orderId}`) != null;
}
