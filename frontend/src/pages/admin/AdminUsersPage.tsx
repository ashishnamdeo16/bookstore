import { useCallback, useEffect, useState } from 'react';
import { userService } from '../../api/userService';
import { AlertBanner } from '../../components/books/AlertBanner';
import { ConfirmDialog, LoadingBlock } from '../../components/books/ConfirmDialog';
import { EmptyState } from '../../components/ui/EmptyState';
import { PageHeader } from '../../components/ui/PageHeader';
import { ApiError } from '../../types/api';
import type { UserProfile } from '../../types/user';

export function AdminUsersPage() {
  const [users, setUsers] = useState<UserProfile[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [query, setQuery] = useState('');

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setUsers(await userService.getAll());
    } catch (err) {
      setError(
        err instanceof ApiError
          ? err.message
          : 'Unable to load users. Admin access is required.',
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const filtered = users.filter((user) => {
    const q = query.trim().toLowerCase();
    if (!q) return true;
    return (
      (user.firstName ?? '').toLowerCase().includes(q) ||
      (user.lastName ?? '').toLowerCase().includes(q) ||
      (user.email ?? '').toLowerCase().includes(q) ||
      (user.phoneNumber ?? '').includes(q)
    );
  });

  async function handleDelete() {
    if (!deleteId) return;
    setDeleting(true);
    try {
      await userService.remove(deleteId);
      setDeleteId(null);
      await refresh();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Unable to delete user.');
    } finally {
      setDeleting(false);
    }
  }

  if (loading) return <LoadingBlock label="Loading users" />;

  return (
    <div className="page-stack">
      <PageHeader
        title="User management"
        subtitle="Profiles from the User Service. Full auth administration comes later."
      />

      {error ? <AlertBanner variant="error">{error}</AlertBanner> : null}

      <label className="md-search max-w-xl">
        <span className="md-search__icon" aria-hidden="true">
          ⌕
        </span>
        <input
          type="search"
          placeholder="Search users"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
        />
      </label>

      {users.length === 0 ? (
        <EmptyState
          title="No users found"
          description="User profiles will appear here when available."
        />
      ) : filtered.length === 0 ? (
        <EmptyState title="No matches" description="Try another search." />
      ) : (
        <div className="md-table-wrap">
          <table className="md-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>User ID</th>
                <th aria-label="Actions" />
              </tr>
            </thead>
            <tbody>
              {filtered.map((user) => (
                <tr key={user.userId}>
                  <td>
                    {[user.firstName, user.lastName].filter(Boolean).join(' ') || '—'}
                  </td>
                  <td>{user.email || '—'}</td>
                  <td>{user.phoneNumber || '—'}</td>
                  <td>
                    <code className="text-xs">{user.userId}</code>
                  </td>
                  <td>
                    <div className="md-table__actions">
                      <button
                        type="button"
                        className="md-icon-btn md-icon-btn--danger"
                        onClick={() => setDeleteId(user.userId)}
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <div className="placeholder-panel">
        <h2>More user admin tools coming soon</h2>
        <p>
          Role changes and auth-account management stay in the Auth Service. This view lists User
          Service profiles via existing APIs.
        </p>
      </div>

      <ConfirmDialog
        open={Boolean(deleteId)}
        title="Delete user profile?"
        message="This deletes the user profile record from the User Service."
        loading={deleting}
        onCancel={() => setDeleteId(null)}
        onConfirm={() => void handleDelete()}
      />
    </div>
  );
}
