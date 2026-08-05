import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { orderService } from '../../api/orderService';
import { AlertBanner } from '../../components/books/AlertBanner';
import { LoadingBlock } from '../../components/books/ConfirmDialog';
import { OrderStatusBadge } from '../../components/orders/OrderStatusBadge';
import { Button } from '../../components/ui/Button';
import { EmptyState } from '../../components/ui/EmptyState';
import { PageHeader } from '../../components/ui/PageHeader';
import { formatDateTime, formatPrice } from '../../features/orders/orderFormat';
import { ApiError } from '../../types/api';
import type { OrderResponse } from '../../types/order';

export function OrdersPage() {
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await orderService.getMine();
      setOrders(
        [...data].sort((a, b) => {
          const left = a.createdAt ? new Date(a.createdAt).getTime() : 0;
          const right = b.createdAt ? new Date(b.createdAt).getTime() : 0;
          return right - left;
        }),
      );
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not load orders.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  useEffect(() => {
    function onVisible() {
      if (document.visibilityState === 'visible') {
        void load();
      }
    }
    document.addEventListener('visibilitychange', onVisible);
    window.addEventListener('focus', onVisible);
    return () => {
      document.removeEventListener('visibilitychange', onVisible);
      window.removeEventListener('focus', onVisible);
    };
  }, [load]);

  if (loading) {
    return <LoadingBlock label="Loading orders" />;
  }

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Account"
        title="My orders"
        subtitle={
          orders.length > 0
            ? `${orders.length} order${orders.length === 1 ? '' : 's'} in your history`
            : 'Track purchases and order history.'
        }
        actions={
          <Button variant="secondary" size="sm" onClick={() => void load()}>
            Refresh
          </Button>
        }
      />

      {error ? (
        <AlertBanner variant="error" title="Couldn’t load orders">
          {error}
        </AlertBanner>
      ) : null}

      {!error && orders.length === 0 ? (
        <EmptyState
          title="No orders yet"
          description="When you place an order, it will show up here."
          actionLabel="Browse books"
          actionTo="/books"
        />
      ) : (
        <ul className="order-list">
          {orders.map((order) => {
            const itemCount = order.items.reduce((sum, item) => sum + item.quantity, 0);
            return (
              <li key={order.orderId}>
                <Link to={`/orders/${order.orderId}`} className="order-card">
                  <div className="order-card__main">
                    <p className="order-card__id">Order {order.orderId.slice(0, 8)}…</p>
                    <p className="order-card__date">{formatDateTime(order.createdAt)}</p>
                    <p className="order-card__items">
                      {itemCount} item{itemCount === 1 ? '' : 's'}
                      {order.items[0] ? ` · ${order.items[0].bookTitle}` : ''}
                      {order.items.length > 1 ? ` +${order.items.length - 1} more` : ''}
                    </p>
                  </div>
                  <div className="order-card__side">
                    <OrderStatusBadge status={order.status} />
                    <strong className="order-card__total">
                      {formatPrice(Number(order.totalAmount))}
                    </strong>
                    <span className="order-card__cta">View</span>
                  </div>
                </Link>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}
