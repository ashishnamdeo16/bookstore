import { useCallback, useEffect, useState } from 'react';
import { userService } from '../../api/userService';
import { useAuth } from '../../auth/AuthProvider';
import { AlertBanner } from '../../components/books/AlertBanner';
import { ConfirmDialog, LoadingBlock } from '../../components/books/ConfirmDialog';
import { Button } from '../../components/ui/Button';
import { EmptyState } from '../../components/ui/EmptyState';
import { PageHeader } from '../../components/ui/PageHeader';
import { toast } from '../../components/ui/toast';
import { ApiError } from '../../types/api';
import type { UserProfile } from '../../types/user';

export function AdminUsersPage() {
  const { user: currentUser } = useAuth();
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
    if (currentUser?.userId && deleteId === currentUser.userId) {
      setDeleteId(null);
      setError('You cannot delete your own profile from user management.');
      return;
    }
    setDeleting(true);
    try {
      await userService.remove(deleteId);
      setDeleteId(null);
      toast.success('User profile deleted');
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
        eyebrow="Admin"
        title="User management"
        subtitle={`${filtered.length} of ${users.length} profiles`}
        actions={
          <Button variant="secondary" size="sm" onClick={() => void refresh()}>
            Refresh
          </Button>
        }
      />

      {error ? <AlertBanner variant="error">{error}</AlertBanner> : null}

      <div className="browse-toolbar">
        <label className="md-search browse-toolbar__search">
          <span className="md-search__icon" aria-hidden="true">
            ⌕
          </span>
          <input
            type="search"
            placeholder="Search name, email, or phone…"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            aria-label="Search users"
          />
        </label>
      </div>

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
              {filtered.map((user) => {
                const isSelf = Boolean(currentUser?.userId && user.userId === currentUser.userId);
                return (
                  <tr key={user.userId}>
                    <td>
                      <span className="admin-users__name">
                        {[user.firstName, user.lastName].filter(Boolean).join(' ') || '—'}
                        {isSelf ? <span className="role-badge role-badge--quiet">You</span> : null}
                      </span>
                    </td>
                    <td>{user.email || '—'}</td>
                    <td>{user.phoneNumber || '—'}</td>
                    <td>
                      <code className="admin-mono">{user.userId}</code>
                    </td>
                    <td>
                      <div className="md-table__actions">
                        {isSelf ? (
                          <span className="admin-users__self-note">Your account</span>
                        ) : (
                          <button
                            type="button"
                            className="md-icon-btn md-icon-btn--danger"
                            onClick={() => setDeleteId(user.userId)}
                          >
                            Delete
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

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
