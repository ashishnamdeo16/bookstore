import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import {
  CardElement,
  Elements,
  useElements,
  useStripe,
} from '@stripe/react-stripe-js';
import { loadStripe, type StripeCardElementOptions } from '@stripe/stripe-js';
import { paymentService } from '../../api/paymentService';
import { AlertBanner } from '../../components/books/AlertBanner';
import { LoadingBlock } from '../../components/books/ConfirmDialog';
import { CheckoutSteps } from '../../components/checkout/CheckoutSteps';
import { Button } from '../../components/ui/Button';
import { PageHeader } from '../../components/ui/PageHeader';
import { useCart } from '../../features/cart/CartContext';
import { formatPrice } from '../../features/orders/orderFormat';
import { useTheme } from '../../theme/ThemeProvider';
import { ApiError } from '../../types/api';
import type { PaymentResponse } from '../../types/payment';

const publishableKey = import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY ?? '';
const stripePromise = publishableKey ? loadStripe(publishableKey) : null;

function useCardOptions(): StripeCardElementOptions {
  const { resolvedTheme } = useTheme();
  const isDark = resolvedTheme === 'dark';

  return useMemo(
    () => ({
      style: {
        base: {
          fontSize: '16px',
          color: isDark ? '#f5f0e8' : '#1a1612',
          fontFamily: 'DM Sans, Segoe UI, system-ui, sans-serif',
          '::placeholder': { color: isDark ? '#b8aea2' : '#6b635a' },
          iconColor: isDark ? '#a898d8' : '#5b4b8a',
        },
        invalid: { color: isDark ? '#ef9a9a' : '#c62828' },
      },
    }),
    [isDark],
  );
}

function PaymentForm({ payment }: { payment: PaymentResponse }) {
  const stripe = useStripe();
  const elements = useElements();
  const navigate = useNavigate();
  const { clear } = useCart();
  const cardOptions = useCardOptions();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handlePay(event: FormEvent) {
    event.preventDefault();
    if (!stripe || !elements) return;

    const card = elements.getElement(CardElement);
    if (!card) {
      setError('Card form is not ready. Please refresh and try again.');
      return;
    }

    setSubmitting(true);
    setError(null);
    const result = await stripe.confirmCardPayment(payment.clientSecret, {
      payment_method: { card },
    });

    if (result.error) {
      setSubmitting(false);
      navigate(`/checkout/payment/${payment.paymentId}/failed`, {
        replace: true,
        state: { message: result.error.message },
      });
      return;
    }

    if (result.paymentIntent?.status === 'succeeded') {
      clear();
      navigate(`/payment-success?paymentId=${payment.paymentId}`, { replace: true });
      return;
    }

    setError(`Unexpected payment status: ${result.paymentIntent?.status ?? 'unknown'}`);
    setSubmitting(false);
  }

  return (
    <>
      {submitting ? (
        <div className="payment-overlay" role="status" aria-live="assertive">
          <div className="checkout-result checkout-result--processing payment-overlay__card">
            <div className="payment-processing" aria-hidden="true">
              <span className="payment-processing__orbit" />
              <span className="payment-processing__orbit payment-processing__orbit--delayed" />
              <span className="payment-processing__core" />
            </div>
            <h2 className="checkout-result__title">Processing payment</h2>
            <p className="checkout-result__body">
              Contacting Stripe — don’t close or refresh this page.
            </p>
          </div>
        </div>
      ) : null}
      <form className="checkout-panel payment-form" onSubmit={(event) => void handlePay(event)}>
        <h2 className="checkout-panel__title">Card details</h2>
        <p className="payment-form__hint">
          Test card <code>4242 4242 4242 4242</code> · any future expiry · any CVC
        </p>
        <div className="payment-form__card">
          <CardElement options={cardOptions} />
        </div>
        {error ? (
          <AlertBanner variant="error" title="Payment error">
            {error}
          </AlertBanner>
        ) : null}
        <div className="payment-form__actions">
          <div>
            <p className="payment-form__amount-label">Amount due</p>
            <p className="payment-form__amount">{formatPrice(Number(payment.amount))}</p>
          </div>
          <Button type="submit" variant="primary" size="lg" loading={submitting} disabled={!stripe}>
            Pay now
          </Button>
        </div>
      </form>
    </>
  );
}

export function PaymentPage() {
  const { paymentId } = useParams<{ paymentId: string }>();
  const navigate = useNavigate();
  const [payment, setPayment] = useState<PaymentResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!paymentId) return;
    let cancelled = false;

    async function load() {
      try {
        const result = await paymentService.getById(paymentId!);
        if (cancelled) return;
        if (result.status === 'SUCCESS') {
          navigate(`/payment-success?paymentId=${paymentId}`, { replace: true });
          return;
        }
        setPayment(result);
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof ApiError ? err.message : 'Could not prepare payment. Please try again.',
          );
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    void load();
    return () => {
      cancelled = true;
    };
  }, [navigate, paymentId]);

  if (!publishableKey || !stripePromise) {
    return (
      <AlertBanner variant="error" title="Stripe is not configured">
        Set <code>VITE_STRIPE_PUBLISHABLE_KEY</code> to your Stripe test publishable key.
      </AlertBanner>
    );
  }

  if (loading) return <LoadingBlock label="Preparing secure payment" />;

  if (error || !payment) {
    return (
      <div className="page-stack">
        <AlertBanner variant="error" title="Payment unavailable">
          {error ?? 'Payment details are not available.'}
        </AlertBanner>
        <Link to="/cart" className="btn btn--secondary w-fit">
          Return to cart
        </Link>
      </div>
    );
  }

  return (
    <div className="page-stack">
      <PageHeader
        backTo="/checkout"
        backLabel="Review checkout"
        eyebrow="Checkout"
        title="Secure payment"
        subtitle={`Payment ${payment.paymentId.slice(0, 8)}…`}
      />
      <CheckoutSteps current="payment" />
      <div className="checkout-layout">
        <section className="checkout-panel">
          <h2 className="checkout-panel__title">Order summary</h2>
          <ul className="cart-list">
            {payment.items.map((item) => (
              <li key={item.bookId} className="cart-list__row">
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
          <div className="cart-summary">
            <span>Total</span>
            <strong>{formatPrice(Number(payment.amount))}</strong>
          </div>
        </section>
        <aside className="checkout-aside">
          <Elements stripe={stripePromise}>
            <PaymentForm payment={payment} />
          </Elements>
        </aside>
      </div>
    </div>
  );
}
