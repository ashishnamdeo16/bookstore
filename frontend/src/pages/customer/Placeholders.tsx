import { PageHeader } from '../../components/ui/PageHeader';

export function CartPlaceholderPage() {
  return (
    <div className="page-stack">
      <PageHeader title="Cart" subtitle="Your shopping cart will appear here." />
      <div className="placeholder-panel">
        <h2>Cart coming soon</h2>
        <p>Checkout and cart management will be connected in a later release.</p>
      </div>
    </div>
  );
}

export function OrdersPlaceholderPage() {
  return (
    <div className="page-stack">
      <PageHeader title="My Orders" subtitle="Track purchases and order history." />
      <div className="placeholder-panel">
        <h2>Orders coming soon</h2>
        <p>Order history will appear here once the order service is available.</p>
      </div>
    </div>
  );
}
