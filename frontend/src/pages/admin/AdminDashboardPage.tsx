import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { userService } from '../../api/userService';
import { LoadingBlock } from '../../components/books/ConfirmDialog';
import { StatCard } from '../../components/dashboard/StatCard';
import { PageHeader } from '../../components/ui/PageHeader';
import { useBookLookups, useBooks } from '../../hooks/useBooks';

export function AdminDashboardPage() {
  const { books, loading } = useBooks();
  const lookups = useBookLookups();
  const [userCount, setUserCount] = useState<number | null>(null);

  useEffect(() => {
    void (async () => {
      try {
        const users = await userService.getAll();
        setUserCount(users.length);
      } catch {
        setUserCount(null);
      }
    })();
  }, []);

  if (loading || lookups.loading) {
    return <LoadingBlock label="Loading admin dashboard" />;
  }

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Admin Portal"
        title="Dashboard"
        subtitle="Manage catalog entities and monitor bookstore activity."
      />

      <div className="stat-grid">
        <StatCard label="Books" value={books.length} to="/admin/books" hint="Manage catalog" />
        <StatCard
          label="Authors"
          value={lookups.authors.length}
          to="/admin/authors"
          hint="Manage authors"
        />
        <StatCard
          label="Publishers"
          value={lookups.publishers.length}
          to="/admin/publishers"
          hint="Manage publishers"
        />
        <StatCard
          label="Categories"
          value={lookups.categories.length}
          to="/admin/categories"
          hint="Manage categories"
        />
        <StatCard label="Users" value={userCount ?? '—'} to="/admin/users" hint="User management" />
      </div>

      <section className="dashboard-section">
        <div className="dashboard-section__header">
          <h2 className="dashboard-section__title">Admin actions</h2>
          <Link to="/admin/analytics" className="md-back">
            Analytics →
          </Link>
        </div>
        <div className="dashboard-actions">
          <Link to="/admin/books/new" className="btn btn--primary">
            Add book
          </Link>
          <Link to="/admin/books" className="btn btn--secondary">
            Manage books
          </Link>
          <Link to="/admin/authors" className="btn btn--secondary">
            Authors
          </Link>
          <Link to="/admin/users" className="btn btn--secondary">
            Users
          </Link>
        </div>
      </section>
    </div>
  );
}
