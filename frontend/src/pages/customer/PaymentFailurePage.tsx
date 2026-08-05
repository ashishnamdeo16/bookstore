import { Link, useLocation, useParams } from 'react-router-dom';
import { PageHeader } from '../../components/ui/PageHeader';

export function PaymentFailurePage() {
  const { paymentId } = useParams<{ paymentId: string }>();
  const location = useLocation();
  const message =
    (location.state as { message?: string } | null)?.message ??
    'Your card could not be charged. No payment was completed.';

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Checkout"
        title="Payment failed"
        subtitle={paymentId ? `Payment ${paymentId.slice(0, 8)}…` : undefined}
      />

      <div className="checkout-result checkout-result--error">
        <div className="checkout-result__icon" aria-hidden="true">
          !
        </div>
        <h2 className="checkout-result__title">We couldn’t complete payment</h2>
        <p className="checkout-result__body">{message}</p>

        <div className="checkout-result__actions">
          {paymentId ? (
            <Link to={`/checkout/payment/${paymentId}`} className="btn btn--primary">
              Try again
            </Link>
          ) : null}
          <Link to="/cart" className="btn btn--ghost">
            Return to cart
          </Link>
        </div>
      </div>
    </div>
  );
}
