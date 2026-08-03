import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { AlertBanner } from '../../components/books/AlertBanner';
import { ConfirmDialog, LoadingBlock, SectionCard } from '../../components/books/ConfirmDialog';
import { EmptyState } from '../../components/ui/EmptyState';
import { PageHeader } from '../../components/ui/PageHeader';
import { ApiError } from '../../types/api';
import { authorService, type AuthorRequest } from '../../services/authorService';
import type { Author } from '../../types/book';

const emptyForm: AuthorRequest = {
  firstName: '',
  lastName: '',
  biography: '',
  country: '',
};

export function AdminAuthorsPage() {
  const [items, setItems] = useState<Author[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [form, setForm] = useState<AuthorRequest>(emptyForm);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [deleting, setDeleting] = useState(false);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setItems(await authorService.getAll());
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Unable to load authors.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    if (!form.firstName.trim()) return;
    setSaving(true);
    setError(null);
    try {
      const payload = {
        firstName: form.firstName.trim(),
        lastName: form.lastName?.trim() || undefined,
        biography: form.biography?.trim() || undefined,
        country: form.country?.trim() || undefined,
      };
      if (editingId) {
        await authorService.update(editingId, payload);
      } else {
        await authorService.create(payload);
      }
      setForm(emptyForm);
      setEditingId(null);
      await refresh();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Unable to save author.');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    setDeleting(true);
    try {
      await authorService.remove(deleteId);
      setDeleteId(null);
      await refresh();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Unable to delete author.');
    } finally {
      setDeleting(false);
    }
  }

  if (loading) return <LoadingBlock label="Loading authors" />;

  return (
    <div className="page-stack">
      <PageHeader title="Authors" subtitle="Create and maintain author records." />
      {error ? <AlertBanner variant="error">{error}</AlertBanner> : null}

      <SectionCard>
        <h2 className="md-page-title mb-4" style={{ fontSize: 18 }}>
          {editingId ? 'Edit author' : 'Add author'}
        </h2>
        <form className="grid gap-4 md:grid-cols-2" onSubmit={(e) => void handleSubmit(e)}>
          <label className="field">
            <span className="field__label">First name</span>
            <input
              className="field__input"
              value={form.firstName}
              onChange={(e) => setForm((f) => ({ ...f, firstName: e.target.value }))}
              required
            />
          </label>
          <label className="field">
            <span className="field__label">Last name</span>
            <input
              className="field__input"
              value={form.lastName}
              onChange={(e) => setForm((f) => ({ ...f, lastName: e.target.value }))}
            />
          </label>
          <label className="field">
            <span className="field__label">Country</span>
            <input
              className="field__input"
              value={form.country}
              onChange={(e) => setForm((f) => ({ ...f, country: e.target.value }))}
            />
          </label>
          <label className="field md:col-span-2">
            <span className="field__label">Biography</span>
            <textarea
              className="field__input field__textarea"
              value={form.biography}
              onChange={(e) => setForm((f) => ({ ...f, biography: e.target.value }))}
            />
          </label>
          <div className="md:col-span-2 flex gap-2">
            <button type="submit" className="btn btn--primary" disabled={saving}>
              {saving ? 'Saving…' : editingId ? 'Update author' : 'Create author'}
            </button>
            {editingId ? (
              <button
                type="button"
                className="btn btn--secondary"
                onClick={() => {
                  setEditingId(null);
                  setForm(emptyForm);
                }}
              >
                Cancel
              </button>
            ) : null}
          </div>
        </form>
      </SectionCard>

      {items.length === 0 ? (
        <EmptyState title="No authors" description="Add an author to get started." />
      ) : (
        <div className="md-table-wrap">
          <table className="md-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Country</th>
                <th aria-label="Actions" />
              </tr>
            </thead>
            <tbody>
              {items.map((author) => (
                <tr key={author.id}>
                  <td>
                    {author.firstName} {author.lastName}
                  </td>
                  <td>{author.country || '—'}</td>
                  <td>
                    <div className="md-table__actions">
                      <button
                        type="button"
                        className="md-icon-btn"
                        onClick={() => {
                          setEditingId(author.id);
                          setForm({
                            firstName: author.firstName,
                            lastName: author.lastName ?? '',
                            biography: author.biography ?? '',
                            country: author.country ?? '',
                          });
                        }}
                      >
                        Edit
                      </button>
                      <button
                        type="button"
                        className="md-icon-btn md-icon-btn--danger"
                        onClick={() => setDeleteId(author.id)}
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

      <ConfirmDialog
        open={Boolean(deleteId)}
        title="Delete author?"
        message="This removes the author record."
        loading={deleting}
        onCancel={() => setDeleteId(null)}
        onConfirm={() => void handleDelete()}
      />
    </div>
  );
}
