import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { AlertBanner } from '../../components/books/AlertBanner';
import { ConfirmDialog, LoadingBlock, SectionCard } from '../../components/books/ConfirmDialog';
import { EmptyState } from '../../components/ui/EmptyState';
import { PageHeader } from '../../components/ui/PageHeader';
import { ApiError } from '../../types/api';
import { publisherService, type PublisherRequest } from '../../services/publisherService';
import type { Publisher } from '../../types/book';

const emptyForm: PublisherRequest = { name: '', address: '' };

export function AdminPublishersPage() {
  const [items, setItems] = useState<Publisher[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [form, setForm] = useState<PublisherRequest>(emptyForm);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [deleting, setDeleting] = useState(false);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setItems(await publisherService.getAll());
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Unable to load publishers.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    if (!form.name.trim()) return;
    setSaving(true);
    setError(null);
    try {
      const payload = {
        name: form.name.trim(),
        address: form.address?.trim() || undefined,
      };
      if (editingId) await publisherService.update(editingId, payload);
      else await publisherService.create(payload);
      setForm(emptyForm);
      setEditingId(null);
      await refresh();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Unable to save publisher.');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    setDeleting(true);
    try {
      await publisherService.remove(deleteId);
      setDeleteId(null);
      await refresh();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Unable to delete publisher.');
    } finally {
      setDeleting(false);
    }
  }

  if (loading) return <LoadingBlock label="Loading publishers" />;

  return (
    <div className="page-stack">
      <PageHeader title="Publishers" subtitle="Manage publishing houses." />
      {error ? <AlertBanner variant="error">{error}</AlertBanner> : null}

      <SectionCard>
        <h2 className="md-page-title mb-4" style={{ fontSize: 18 }}>
          {editingId ? 'Edit publisher' : 'Add publisher'}
        </h2>
        <form className="grid gap-4 md:grid-cols-2" onSubmit={(e) => void handleSubmit(e)}>
          <label className="field">
            <span className="field__label">Name</span>
            <input
              className="field__input"
              value={form.name}
              onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
              required
            />
          </label>
          <label className="field">
            <span className="field__label">Address</span>
            <input
              className="field__input"
              value={form.address}
              onChange={(e) => setForm((f) => ({ ...f, address: e.target.value }))}
            />
          </label>
          <div className="md:col-span-2 flex gap-2">
            <button type="submit" className="btn btn--primary" disabled={saving}>
              {saving ? 'Saving…' : editingId ? 'Update publisher' : 'Create publisher'}
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
        <EmptyState title="No publishers" description="Add a publisher to get started." />
      ) : (
        <div className="md-table-wrap">
          <table className="md-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Address</th>
                <th aria-label="Actions" />
              </tr>
            </thead>
            <tbody>
              {items.map((publisher) => (
                <tr key={publisher.id}>
                  <td>{publisher.name}</td>
                  <td>{publisher.address || '—'}</td>
                  <td>
                    <div className="md-table__actions">
                      <button
                        type="button"
                        className="md-icon-btn"
                        onClick={() => {
                          setEditingId(publisher.id);
                          setForm({
                            name: publisher.name,
                            address: publisher.address ?? '',
                          });
                        }}
                      >
                        Edit
                      </button>
                      <button
                        type="button"
                        className="md-icon-btn md-icon-btn--danger"
                        onClick={() => setDeleteId(publisher.id)}
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
        title="Delete publisher?"
        message="This removes the publisher record."
        loading={deleting}
        onCancel={() => setDeleteId(null)}
        onConfirm={() => void handleDelete()}
      />
    </div>
  );
}
