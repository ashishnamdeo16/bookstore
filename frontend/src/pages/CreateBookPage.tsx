import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AlertBanner } from '../components/books/AlertBanner';
import { BookForm } from '../components/books/BookForm';
import { LoadingBlock, SectionCard } from '../components/books/ConfirmDialog';
import { PageHeader } from '../components/ui/PageHeader';
import { useBookLookups } from '../hooks/useBooks';
import { bookService } from '../services/bookService';
import { ApiError } from '../types/api';
import type { BookCreateRequest } from '../types/book';

export function CreateBookPage() {
  const navigate = useNavigate();
  const lookups = useBookLookups();
  const [saving, setSaving] = useState(false);

  async function handleSubmit(payload: BookCreateRequest, coverFile: File | null) {
    setSaving(true);
    try {
      const created = await bookService.create(payload);
      if (coverFile) {
        await bookService.uploadCover(created.id, coverFile);
      }
      navigate(`/books/${created.id}`, { replace: true });
    } catch (err) {
      throw new Error(err instanceof ApiError ? err.message : 'Unable to create book.');
    } finally {
      setSaving(false);
    }
  }

  if (lookups.loading) {
    return <LoadingBlock label="Loading form" />;
  }

  if (lookups.error) {
    return (
      <AlertBanner variant="error" title="Couldn’t load options">
        {lookups.error}
      </AlertBanner>
    );
  }

  return (
    <div className="mx-auto max-w-3xl page-stack">
      <PageHeader
        backTo="/books"
        backLabel="Books"
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
          onCancel={() => navigate('/books')}
        />
      </SectionCard>
    </div>
  );
}
