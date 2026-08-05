import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { AlertBanner } from '../../components/books/AlertBanner';
import { ConfirmDialog, LoadingBlock, SectionCard } from '../../components/books/ConfirmDialog';
import { Button } from '../../components/ui/Button';
import { EmptyState } from '../../components/ui/EmptyState';
import { PageHeader } from '../../components/ui/PageHeader';
import { toast } from '../../components/ui/toast';
import { publisherService, type PublisherRequest } from '../../services/publisherService';
import { ApiError } from '../../types/api';
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
      if (editingId) {
        await publisherService.update(editingId, payload);
        toast.success('Publisher updated');
      } else {
        await publisherService.create(payload);
        toast.success('Publisher created');
      }
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
      toast.success('Publisher deleted');
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
      <PageHeader
        eyebrow="Catalog"
        title="Publishers"
        subtitle={`${items.length} publisher${items.length === 1 ? '' : 's'} in the catalog`}
      />
      {error ? <AlertBanner variant="error">{error}</AlertBanner> : null}

      <div className="admin-entity-layout">
        <SectionCard className="admin-entity-form">
          <h2 className="admin-entity-form__title">
            {editingId ? 'Edit publisher' : 'Add publisher'}
          </h2>
          <form className="admin-form" onSubmit={(e) => void handleSubmit(e)}>
            <div className="admin-form__grid">
              <label className="field">
                <span className="field__label">
                  Name <span className="field__required">*</span>
                </span>
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
            </div>
            <div className="admin-form__actions">
              {editingId ? (
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => {
                    setEditingId(null);
                    setForm(emptyForm);
                  }}
                >
                  Cancel
                </Button>
              ) : null}
              <Button type="submit" loading={saving}>
                {editingId ? 'Update publisher' : 'Create publisher'}
              </Button>
            </div>
          </form>
        </SectionCard>

        <div className="admin-entity-list">
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
        </div>
      </div>

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
