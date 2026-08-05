import type { OrderStatus } from '../../types/order';

interface OrderStatusBadgeProps {
  status: OrderStatus | string;
}

export function OrderStatusBadge({ status }: OrderStatusBadgeProps) {
  const key = status.toLowerCase().replace(/_/g, '-');
  return <span className={`status-pill status-pill--${key}`}>{status.replace(/_/g, ' ')}</span>;
}
