import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AlertBanner } from '../../components/books/AlertBanner';
import { BookForm } from '../../components/books/BookForm';
import { LoadingBlock, SectionCard } from '../../components/books/ConfirmDialog';
import { PageHeader } from '../../components/ui/PageHeader';
import { toast } from '../../components/ui/toast';
import { useBookLookups } from '../../hooks/useBooks';
import { bookService } from '../../services/bookService';
import { ApiError } from '../../types/api';
import type { BookCreateRequest } from '../../types/book';

export function AdminCreateBookPage() {
  const navigate = useNavigate();
  const lookups = useBookLookups();
  const [saving, setSaving] = useState(false);

  async function handleSubmit(payload: BookCreateRequest, coverFile: File | null) {
    setSaving(true);
    try {
      const book = await bookService.create(payload);
      if (coverFile) {
        await bookService.uploadCover(book.id, coverFile);
      }
      toast.success(coverFile ? 'Book created with cover' : 'Book created');
      navigate('/admin/books', { replace: true });
    } catch (err) {
      throw new Error(err instanceof ApiError ? err.message : 'Unable to create book.');
    } finally {
      setSaving(false);
    }
  }

  if (lookups.loading) return <LoadingBlock label="Loading form" />;
  if (lookups.error) {
    return (
      <AlertBanner variant="error" title="Couldn’t load options">
        {lookups.error}
      </AlertBanner>
    );
  }

  return (
    <div className="page-stack admin-book-form-page">
      <PageHeader
        backTo="/admin/books"
        backLabel="Books"
        eyebrow="Catalog"
        title="Add book"
        subtitle="Create a new title in the catalog."
      />
      <SectionCard>
        <BookForm
          authors={lookups.authors}
          categories={lookups.categories}
          publishers={lookups.publishers}
          submitLabel="Create book"
          loading={saving}
          onSubmit={handleSubmit}
          onCancel={() => navigate('/admin/books')}
        />
      </SectionCard>
    </div>
  );
}
