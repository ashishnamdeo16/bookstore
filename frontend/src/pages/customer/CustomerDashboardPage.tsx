import { Link } from 'react-router-dom';
import { useAuth } from '../../auth/AuthProvider';
import { StatCard } from '../../components/dashboard/StatCard';
import { LoadingBlock } from '../../components/books/ConfirmDialog';
import { PageHeader } from '../../components/ui/PageHeader';
import { useBooks } from '../../hooks/useBooks';
import { useProfile } from '../../features/profile/useProfile';

export function CustomerDashboardPage() {
  const { user } = useAuth();
  const { books, loading } = useBooks();
  const { profile, loading: profileLoading } = useProfile(user?.userId);

  if (loading || profileLoading) {
    return <LoadingBlock label="Loading dashboard" />;
  }

  const firstName = profile?.firstName?.trim() || 'there';

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Customer Portal"
        title={`Welcome, ${firstName}`}
        subtitle="Browse the catalog, manage your profile, and track your account."
      />

      <div className="stat-grid">
        <StatCard label="Books available" value={books.length} to="/books" hint="Browse catalog" />
        <StatCard label="Cart items" value="—" to="/cart" hint="Coming soon" />
        <StatCard label="Orders" value="—" to="/orders" hint="Coming soon" />
        <StatCard label="Profile" value="Ready" to="/profile" hint="View account" />
      </div>

      <section className="md-card">
        <h2 className="md-page-title" style={{ fontSize: 18 }}>
          Quick actions
        </h2>
        <div className="mt-4 flex flex-wrap gap-2">
          <Link to="/books" className="btn btn--primary">
            Browse books
          </Link>
          <Link to="/profile" className="btn btn--secondary">
            View profile
          </Link>
          <Link to="/profile/edit" className="btn btn--secondary">
            Edit profile
          </Link>
        </div>
      </section>
    </div>
  );
}
