import { useEffect, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { orderService } from '../../api/orderService';
import { AlertBanner } from '../../components/books/AlertBanner';
import { Spinner } from '../../components/ui/Spinner';
import { ApiError } from '../../types/api';

const POLL_INTERVAL_MS = 1500;
const POLL_TIMEOUT_MS = 60000;

export function PaymentSuccessPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const paymentId = searchParams.get('paymentId');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!paymentId) return;
    let cancelled = false;
    let timeoutId: number | undefined;
    const startedAt = Date.now();

    async function waitForOrder() {
      try {
        const order = await orderService.getByPaymentId(paymentId!);
        if (!cancelled) {
          navigate(`/orders/${order.orderId}`, {
            replace: true,
            state: { paymentConfirmed: true },
          });
        }
      } catch (err) {
        if (cancelled) return;
        if (Date.now() - startedAt >= POLL_TIMEOUT_MS) {
          setError(
            err instanceof ApiError
              ? err.message
              : 'Payment succeeded, but order creation is taking longer than expected.',
          );
          return;
        }
        timeoutId = window.setTimeout(() => void waitForOrder(), POLL_INTERVAL_MS);
      }
    }

    void waitForOrder();
    return () => {
      cancelled = true;
      if (timeoutId !== undefined) window.clearTimeout(timeoutId);
    };
  }, [navigate, paymentId]);

  if (!paymentId) {
    return (
      <AlertBanner variant="error" title="Missing payment">
        No payment ID was provided.
      </AlertBanner>
    );
  }

  if (error) {
    return (
      <div className="page-stack">
        <AlertBanner variant="info" title="Payment received">
          {error} Your confirmed order will appear in order history shortly.
        </AlertBanner>
        <Link to="/orders" className="btn btn--primary w-fit">
          My orders
        </Link>
      </div>
    );
  }

  return (
    <div className="checkout-result checkout-result--success">
      <div className="checkout-result__icon" aria-hidden="true">
        <Spinner label="Creating your order" />
      </div>
      <h1 className="checkout-result__title">Payment successful</h1>
      <p className="checkout-result__body">
        Stripe confirmed your payment. We’re creating your confirmed order now.
      </p>
    </div>
  );
}
