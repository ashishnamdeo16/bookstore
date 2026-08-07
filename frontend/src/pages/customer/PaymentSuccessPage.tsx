import { useEffect, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { orderService } from '../../api/orderService';
import { AlertBanner } from '../../components/books/AlertBanner';
import { ApiError } from '../../types/api';

const POLL_INTERVAL_MS = 1500;
const POLL_TIMEOUT_MS = 60000;

const STEPS = [
  'Confirming payment with Stripe',
  'Creating your order',
  'Finalizing confirmation',
] as const;

export function PaymentSuccessPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const paymentId = searchParams.get('paymentId');
  const [error, setError] = useState<string | null>(null);
  const [stepIndex, setStepIndex] = useState(0);

  useEffect(() => {
    if (!paymentId || error) return;
    const id = window.setInterval(() => {
      setStepIndex((current) => (current + 1) % STEPS.length);
    }, 2200);
    return () => window.clearInterval(id);
  }, [error, paymentId]);

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
    <div className="checkout-result checkout-result--processing" role="status" aria-live="polite">
      <div className="payment-processing" aria-hidden="true">
        <span className="payment-processing__orbit" />
        <span className="payment-processing__orbit payment-processing__orbit--delayed" />
        <span className="payment-processing__core" />
      </div>
      <h1 className="checkout-result__title">Processing payment</h1>
      <p className="checkout-result__body">
        Please wait — we’re confirming your payment and creating your order.
      </p>
      <p className="payment-processing__step" key={stepIndex}>
        {STEPS[stepIndex]}…
      </p>
      <ul className="payment-processing__dots" aria-hidden="true">
        {STEPS.map((_, index) => (
          <li
            key={STEPS[index]}
            className={`payment-processing__dot ${index === stepIndex ? 'is-active' : ''}`}
          />
        ))}
      </ul>
    </div>
  );
}
