import { useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { CheckoutSteps } from '../../components/checkout/CheckoutSteps';
import { Button } from '../../components/ui/Button';
import { EmptyState } from '../../components/ui/EmptyState';
import { PageHeader } from '../../components/ui/PageHeader';
import { toast } from '../../components/ui/toast';
import { useCart } from '../../features/cart/CartContext';
import { formatPrice } from '../../features/orders/orderFormat';

export function CartPage() {
  const navigate = useNavigate();
  const { items, subtotal, updateQuantity, removeItem, itemCount, maxQuantity } = useCart();
  const [limitedBookId, setLimitedBookId] = useState<string | null>(null);
  const limitTimer = useRef<number | null>(null);

  function handleQuantityChange(bookId: string, rawValue: string) {
    const requested = Number(rawValue) || 1;

    if (requested > maxQuantity) {
      setLimitedBookId(bookId);
      toast.warning(`You can order up to ${maxQuantity} copies of a book in one order.`);
      if (limitTimer.current) window.clearTimeout(limitTimer.current);
      limitTimer.current = window.setTimeout(() => setLimitedBookId(null), 6000);
    } else if (limitedBookId === bookId) {
      setLimitedBookId(null);
    }

    updateQuantity(bookId, requested);
  }

  if (items.length === 0) {
    return (
      <div className="page-stack">
        <PageHeader eyebrow="Checkout" title="Cart" subtitle="Your selected books will appear here." />
        <EmptyState
          title="Your cart is empty"
          description="Browse the catalog and add books to get started."
          actionLabel="Browse books"
          actionTo="/books"
        />
      </div>
    );
  }

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Checkout"
        title="Cart"
        subtitle={`${itemCount} item${itemCount === 1 ? '' : 's'} ready for checkout`}
      />

      <CheckoutSteps current="cart" />

      <div className="checkout-layout">
        <section className="checkout-panel">
          <ul className="cart-list">
            {items.map((item) => (
              <li key={item.bookId} className="cart-list__row">
                <div className="cart-list__info">
                  <div className="cart-list__thumb" aria-hidden="true">
                    {item.title.trim().charAt(0).toUpperCase() || 'B'}
                  </div>
                  <div>
                    <Link to={`/books/${item.bookId}`} className="cart-list__title">
                      {item.title}
                    </Link>
                    <p className="cart-list__unit">{formatPrice(item.price)} each</p>
                  </div>
                </div>
                <div className="cart-list__controls">
                  <label className="cart-qty">
                    <span className="sr-only">Quantity for {item.title}</span>
                    <input
                      type="number"
                      min={1}
                      max={maxQuantity}
                      value={item.quantity}
                      aria-invalid={limitedBookId === item.bookId || undefined}
                      aria-describedby={
                        limitedBookId === item.bookId ? `qty-limit-${item.bookId}` : undefined
                      }
                      onChange={(event) => handleQuantityChange(item.bookId, event.target.value)}
                    />
                  </label>
                  <p className="cart-list__line-total">
                    {formatPrice(item.price * item.quantity)}
                  </p>
                  <Button variant="ghost" size="sm" onClick={() => removeItem(item.bookId)}>
                    Remove
                  </Button>
                </div>
                {limitedBookId === item.bookId ? (
                  <p className="cart-list__limit" id={`qty-limit-${item.bookId}`} role="alert">
                    Maximum {maxQuantity} copies per book in one order — quantity set to{' '}
                    {maxQuantity}.
                  </p>
                ) : null}
              </li>
            ))}
          </ul>
        </section>

        <aside className="checkout-aside">
          <div className="checkout-summary">
            <h2 className="checkout-summary__title">Order summary</h2>
            <div className="checkout-summary__row">
              <span>Items</span>
              <span>{itemCount}</span>
            </div>
            <div className="checkout-summary__row checkout-summary__row--total">
              <span>Subtotal</span>
              <strong>{formatPrice(subtotal)}</strong>
            </div>
            <Button
              variant="primary"
              size="lg"
              fullWidth
              onClick={() => navigate('/checkout')}
            >
              Proceed to checkout
            </Button>
            <Link to="/books" className="checkout-summary__link">
              Continue shopping
            </Link>
          </div>
        </aside>
      </div>
    </div>
  );
}
