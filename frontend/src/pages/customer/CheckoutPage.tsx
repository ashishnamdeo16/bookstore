import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { paymentService } from '../../api/paymentService';
import { AlertBanner } from '../../components/books/AlertBanner';
import { CheckoutSteps } from '../../components/checkout/CheckoutSteps';
import { Button } from '../../components/ui/Button';
import { PageHeader } from '../../components/ui/PageHeader';
import { useCart } from '../../features/cart/CartContext';
import { formatPrice } from '../../features/orders/orderFormat';
import { ApiError } from '../../types/api';

export function CheckoutPage() {
  const navigate = useNavigate();
  const { items, subtotal } = useCart();
  const [checkoutId] = useState(() => crypto.randomUUID());
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (items.length === 0) {
    return <Navigate to="/cart" replace />;
  }

  async function handlePlaceOrder() {
    setSubmitting(true);
    setError(null);
    try {
      const payment = await paymentService.createCheckout({
        checkoutId,
        items: items.map((item) => ({
          bookId: item.bookId,
          quantity: item.quantity,
        })),
      });
      navigate(`/checkout/payment/${payment.paymentId}`, { replace: true });
    } catch (err) {
      const message =
        err instanceof ApiError
          ? err.message
          : 'Could not place your order. Please try again.';
      setError(message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page-stack">
      <PageHeader
        backTo="/cart"
        backLabel="Back to cart"
        eyebrow="Checkout"
        title="Confirm order"
        subtitle="Review your items, then continue to secure Stripe payment."
      />

      <CheckoutSteps current="checkout" />

      {error ? (
        <AlertBanner variant="error" title="Checkout failed">
          {error}
        </AlertBanner>
      ) : null}

      <div className="checkout-layout">
        <section className="checkout-panel">
          <h2 className="checkout-panel__title">Items</h2>
          <ul className="cart-list">
            {items.map((item) => (
              <li key={item.bookId} className="cart-list__row">
                <div className="cart-list__info">
                  <div className="cart-list__thumb" aria-hidden="true">
                    {item.title.trim().charAt(0).toUpperCase() || 'B'}
                  </div>
                  <div>
                    <p className="cart-list__title">{item.title}</p>
                    <p className="cart-list__unit">
                      Qty {item.quantity} · {formatPrice(item.price)} each
                    </p>
                  </div>
                </div>
                <p className="cart-list__line-total">
                  {formatPrice(item.price * item.quantity)}
                </p>
              </li>
            ))}
          </ul>
        </section>

        <aside className="checkout-aside">
          <div className="checkout-summary">
            <h2 className="checkout-summary__title">Payment</h2>
            <div className="checkout-summary__row">
              <span>Items</span>
              <span>{items.reduce((sum, item) => sum + item.quantity, 0)}</span>
            </div>
            <div className="checkout-summary__row checkout-summary__row--total">
              <span>Total</span>
              <strong>{formatPrice(subtotal)}</strong>
            </div>
            <Button
              variant="primary"
              size="lg"
              fullWidth
              loading={submitting}
              onClick={() => void handlePlaceOrder()}
            >
              Continue to payment
            </Button>
            <Link to="/cart" className="checkout-summary__link">
              Edit cart
            </Link>
            <p className="checkout-summary__note">
              You’ll enter card details on the next step. Payment is handled securely by Stripe.
            </p>
          </div>
        </aside>
      </div>
    </div>
  );
}
