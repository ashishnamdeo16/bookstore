interface CheckoutStepsProps {
  current: 'cart' | 'checkout' | 'payment';
}

const STEPS = [
  { id: 'cart', label: 'Cart' },
  { id: 'checkout', label: 'Checkout' },
  { id: 'payment', label: 'Payment' },
] as const;

export function CheckoutSteps({ current }: CheckoutStepsProps) {
  const currentIndex = STEPS.findIndex((step) => step.id === current);

  return (
    <nav className="checkout-steps" aria-label="Checkout progress">
      {STEPS.map((step, index) => {
        const state =
          index < currentIndex ? 'is-done' : index === currentIndex ? 'is-active' : '';
        return (
          <div key={step.id} className="checkout-steps__item">
            {index > 0 ? (
              <span
                className={`checkout-steps__line ${index <= currentIndex ? 'is-active' : ''}`}
                aria-hidden="true"
              />
            ) : null}
            <div className={`checkout-steps__step ${state}`}>
              <span className="checkout-steps__dot" aria-hidden="true">
                {index < currentIndex ? '✓' : index + 1}
              </span>
              <span className="checkout-steps__label">{step.label}</span>
            </div>
          </div>
        );
      })}
    </nav>
  );
}
