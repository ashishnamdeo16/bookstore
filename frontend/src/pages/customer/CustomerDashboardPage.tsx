import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { orderService } from '../../api/orderService';
import { useAuth } from '../../auth/AuthProvider';
import { BookCard } from '../../components/books/BookCard';
import { LoadingBlock } from '../../components/books/ConfirmDialog';
import { StatCard } from '../../components/dashboard/StatCard';
import { PageHeader } from '../../components/ui/PageHeader';
import { useCart } from '../../features/cart/CartContext';
import { useProfile } from '../../features/profile/useProfile';
import { resolveAuthorNames, resolveNameById, useBookLookups, useBooks } from '../../hooks/useBooks';

export function CustomerDashboardPage() {
  const { user } = useAuth();
  const { books, loading } = useBooks();
  const lookups = useBookLookups();
  const { profile, loading: profileLoading } = useProfile(user?.userId);
  const { itemCount } = useCart();
  const [orderCount, setOrderCount] = useState<number | null>(null);

  useEffect(() => {
    let active = true;
    orderService
      .getMine()
      .then((orders) => {
        if (active) setOrderCount(orders.length);
      })
      .catch(() => {
        if (active) setOrderCount(null);
      });
    return () => {
      active = false;
    };
  }, []);

  const featured = useMemo(() => books.slice(0, 4), [books]);

  if (loading || profileLoading || lookups.loading) {
    return <LoadingBlock label="Loading dashboard" />;
  }

  const firstName = profile?.firstName?.trim() || 'there';

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Customer Portal"
        title={`Welcome, ${firstName}`}
        subtitle="Browse the catalog, manage your cart, and track your orders."
      />

      <div className="stat-grid">
        <StatCard label="Books available" value={books.length} to="/books" hint="Browse catalog" />
        <StatCard label="Cart items" value={itemCount} to="/cart" hint="Review cart" />
        <StatCard label="Orders" value={orderCount ?? '—'} to="/orders" hint="View history" />
        <StatCard label="Profile" value="Ready" to="/profile" hint="View account" />
      </div>

      <section className="dashboard-section">
        <div className="dashboard-section__header">
          <h2 className="dashboard-section__title">Quick actions</h2>
        </div>
        <div className="dashboard-actions">
          <Link to="/books" className="btn btn--primary">
            Browse books
          </Link>
          <Link to="/cart" className="btn btn--secondary">
            View cart
          </Link>
          <Link to="/orders" className="btn btn--secondary">
            My orders
          </Link>
          <Link to="/profile" className="btn btn--secondary">
            View profile
          </Link>
        </div>
      </section>

      {featured.length > 0 ? (
        <section className="dashboard-section">
          <div className="dashboard-section__header">
            <h2 className="dashboard-section__title">From the shelf</h2>
            <Link to="/books" className="md-back">
              See all →
            </Link>
          </div>
          <div className="book-grid book-grid--featured">
            {featured.map((book) => (
              <BookCard
                key={book.id}
                book={book}
                authors={resolveAuthorNames(book.authorIds, lookups.authors)}
                category={resolveNameById(book.categoryId, lookups.categories)}
              />
            ))}
          </div>
        </section>
      ) : null}
    </div>
  );
}
