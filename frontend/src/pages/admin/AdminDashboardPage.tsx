import { Link } from 'react-router-dom';
import { LoadingBlock } from '../../components/books/ConfirmDialog';
import { StatCard } from '../../components/dashboard/StatCard';
import { PageHeader } from '../../components/ui/PageHeader';
import { useBookLookups, useBooks } from '../../hooks/useBooks';
import { useEffect, useState } from 'react';
import { userService } from '../../api/userService';

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
        <StatCard
          label="Users"
          value={userCount ?? '—'}
          to="/admin/users"
          hint="User management"
        />
      </div>

      <section className="md-card">
        <h2 className="md-page-title" style={{ fontSize: 18 }}>
          Admin actions
        </h2>
        <div className="mt-4 flex flex-wrap gap-2">
          <Link to="/admin/books/new" className="btn btn--primary">
            Add book
          </Link>
          <Link to="/admin/books" className="btn btn--secondary">
            Manage books
          </Link>
          <Link to="/admin/users" className="btn btn--secondary">
            View users
          </Link>
        </div>
      </section>
    </div>
  );
}
