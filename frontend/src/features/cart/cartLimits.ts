/** Max copies of a single book allowed in one order. */
export const MAX_BOOK_QUANTITY_PER_ORDER = 2000;

export function clampBookQuantity(quantity: number): number {
  if (!Number.isFinite(quantity)) return 1;
  return Math.min(MAX_BOOK_QUANTITY_PER_ORDER, Math.max(1, Math.floor(quantity)));
}
