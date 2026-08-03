import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { AlertBanner } from '../../components/books/AlertBanner';
import { ConfirmDialog, LoadingBlock, SectionCard } from '../../components/books/ConfirmDialog';
import { EmptyState } from '../../components/ui/EmptyState';
import { PageHeader } from '../../components/ui/PageHeader';
import { ApiError } from '../../types/api';
import { categoryService, type CategoryRequest } from '../../services/categoryService';
import type { Category } from '../../types/book';

const emptyForm: CategoryRequest = { name: '', description: '' };

export function AdminCategoriesPage() {
  const [items, setItems] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [form, setForm] = useState<CategoryRequest>(emptyForm);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [deleting, setDeleting] = useState(false);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setItems(await categoryService.getAll());
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Unable to load categories.');
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
        description: form.description?.trim() || undefined,
      };
      if (editingId) await categoryService.update(editingId, payload);
      else await categoryService.create(payload);
      setForm(emptyForm);
      setEditingId(null);
      await refresh();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Unable to save category.');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!deleteId) return;
    setDeleting(true);
    try {
      await categoryService.remove(deleteId);
      setDeleteId(null);
      await refresh();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Unable to delete category.');
    } finally {
      setDeleting(false);
    }
  }

  if (loading) return <LoadingBlock label="Loading categories" />;

  return (
    <div className="page-stack">
      <PageHeader title="Categories" subtitle="Organize the catalog by category." />
      {error ? <AlertBanner variant="error">{error}</AlertBanner> : null}

      <SectionCard>
        <h2 className="md-page-title mb-4" style={{ fontSize: 18 }}>
          {editingId ? 'Edit category' : 'Add category'}
        </h2>
        <form className="grid gap-4" onSubmit={(e) => void handleSubmit(e)}>
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
            <span className="field__label">Description</span>
            <textarea
              className="field__input field__textarea"
              value={form.description}
              onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
            />
          </label>
          <div className="flex gap-2">
            <button type="submit" className="btn btn--primary" disabled={saving}>
              {saving ? 'Saving…' : editingId ? 'Update category' : 'Create category'}
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
        <EmptyState title="No categories" description="Add a category to get started." />
      ) : (
        <div className="md-table-wrap">
          <table className="md-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Description</th>
                <th aria-label="Actions" />
              </tr>
            </thead>
            <tbody>
              {items.map((category) => (
                <tr key={category.id}>
                  <td>{category.name}</td>
                  <td>{category.description || '—'}</td>
                  <td>
                    <div className="md-table__actions">
                      <button
                        type="button"
                        className="md-icon-btn"
                        onClick={() => {
                          setEditingId(category.id);
                          setForm({
                            name: category.name,
                            description: category.description ?? '',
                          });
                        }}
                      >
                        Edit
                      </button>
                      <button
                        type="button"
                        className="md-icon-btn md-icon-btn--danger"
                        onClick={() => setDeleteId(category.id)}
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
        title="Delete category?"
        message="This removes the category record."
        loading={deleting}
        onCancel={() => setDeleteId(null)}
        onConfirm={() => void handleDelete()}
      />
    </div>
  );
}
