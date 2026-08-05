import { useCallback, useEffect, useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import { orderService } from '../../api/orderService';
import { AlertBanner } from '../../components/books/AlertBanner';
import { LoadingBlock } from '../../components/books/ConfirmDialog';
import { OrderStatusBadge } from '../../components/orders/OrderStatusBadge';
import { Button } from '../../components/ui/Button';
import { PageHeader } from '../../components/ui/PageHeader';
import { formatDateTime, formatPrice } from '../../features/orders/orderFormat';
import { ApiError } from '../../types/api';
import type { OrderResponse } from '../../types/order';

export function OrderConfirmationPage() {
  const { orderId } = useParams<{ orderId: string }>();
  const location = useLocation();
  const paymentConfirmed = Boolean(
    (location.state as { paymentConfirmed?: boolean } | null)?.paymentConfirmed,
  );

  const [order, setOrder] = useState<OrderResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [cancelling, setCancelling] = useState(false);

  const load = useCallback(
    async (silent = false) => {
      if (!orderId) return null;
      if (!silent) {
        setLoading(true);
        setError(null);
      }
      try {
        const result = await orderService.getById(orderId);
        setOrder(result);
        return result;
      } catch (err) {
        if (!silent) {
          setError(err instanceof ApiError ? err.message : 'Could not load this order.');
          setOrder(null);
        }
        return null;
      } finally {
        if (!silent) setLoading(false);
      }
    },
    [orderId],
  );

  useEffect(() => {
    void load();
  }, [load]);

  async function handleCancel() {
    if (!orderId) return;
    setCancelling(true);
    try {
      await orderService.cancel(orderId);
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not cancel this order.');
    } finally {
      setCancelling(false);
    }
  }

  if (loading) {
    return <LoadingBlock label="Loading order" />;
  }

  if (error && !order) {
    return (
      <div className="page-stack">
        <AlertBanner variant="error" title="Order not found">
          {error}
        </AlertBanner>
        <Link to="/orders" className="btn btn--secondary w-fit">
          Back to orders
        </Link>
      </div>
    );
  }

  if (!order) {
    return null;
  }

  const canCancel = ['CREATED', 'PENDING', 'PAYMENT_PENDING', 'CONFIRMED'].includes(order.status);

  return (
    <div className="page-stack">
      <PageHeader
        backTo="/orders"
        backLabel="My orders"
        eyebrow="Orders"
        title={paymentConfirmed ? 'Order confirmed' : 'Order details'}
        subtitle={`Order ${order.orderId}`}
        actions={
          <div className="order-detail__actions">
            {canCancel ? (
              <Button variant="danger" loading={cancelling} onClick={() => void handleCancel()}>
                Cancel order
              </Button>
            ) : null}
          </div>
        }
      />

      {paymentConfirmed ? (
        <AlertBanner variant="success" title="Payment successful — order confirmed">
          Stripe confirmed your payment and your order is ready for processing.
        </AlertBanner>
      ) : null}

      {error ? (
        <AlertBanner variant="error" title="Action failed">
          {error}
        </AlertBanner>
      ) : null}

      <div className="order-detail">
        <section className="order-detail__summary">
          <div className="order-detail__stat">
            <span className="order-detail__label">Status</span>
            <OrderStatusBadge status={order.status} />
          </div>
          <div className="order-detail__stat">
            <span className="order-detail__label">Total</span>
            <strong className="order-detail__value">
              {formatPrice(Number(order.totalAmount))}
            </strong>
          </div>
          <div className="order-detail__stat">
            <span className="order-detail__label">Placed</span>
            <span className="order-detail__value order-detail__value--muted">
              {formatDateTime(order.createdAt)}
            </span>
          </div>
        </section>

        <section className="checkout-panel">
          <h2 className="checkout-panel__title">Items</h2>
          <ul className="cart-list">
            {order.items.map((item) => (
              <li key={`${item.bookId}-${item.bookTitle}`} className="cart-list__row">
                <div className="cart-list__info">
                  <div className="cart-list__thumb" aria-hidden="true">
                    {item.bookTitle.trim().charAt(0).toUpperCase() || 'B'}
                  </div>
                  <div>
                    <p className="cart-list__title">{item.bookTitle}</p>
                    <p className="cart-list__unit">
                      Qty {item.quantity} · {formatPrice(Number(item.price))} each
                    </p>
                  </div>
                </div>
                <p className="cart-list__line-total">
                  {formatPrice(Number(item.price) * item.quantity)}
                </p>
              </li>
            ))}
          </ul>
        </section>
      </div>
    </div>
  );
}
