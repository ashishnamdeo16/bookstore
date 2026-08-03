import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { AlertBanner } from '../../components/books/AlertBanner';
import { BookForm } from '../../components/books/BookForm';
import { LoadingBlock, SectionCard } from '../../components/books/ConfirmDialog';
import { PageHeader } from '../../components/ui/PageHeader';
import { useBook, useBookLookups } from '../../hooks/useBooks';
import { bookService } from '../../services/bookService';
import { ApiError } from '../../types/api';
import type { BookCreateRequest } from '../../types/book';

export function AdminEditBookPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { book, loading, error } = useBook(id);
  const lookups = useBookLookups();
  const [saving, setSaving] = useState(false);

  async function handleSubmit(payload: BookCreateRequest) {
    if (!id) return;
    setSaving(true);
    try {
      await bookService.update(id, payload);
      navigate('/admin/books', { replace: true });
    } catch (err) {
      throw new Error(err instanceof ApiError ? err.message : 'Unable to update book.');
    } finally {
      setSaving(false);
    }
  }

  if (loading || lookups.loading) return <LoadingBlock label="Loading book" />;
  if (error || !book) {
    return (
      <AlertBanner variant="error" title="Book not found">
        {error || 'This book could not be loaded.'}
      </AlertBanner>
    );
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
        backTo="/admin/books"
        backLabel="Books"
        title="Edit book"
        subtitle={book.title}
      />
      <SectionCard>
        <BookForm
          initialValues={{
            isbn: book.isbn,
            title: book.title,
            description: book.description ?? '',
            price: String(book.price),
            language: book.language,
            publishedDate: book.publishedDate ?? '',
            categoryId: book.categoryId,
            publisherId: book.publisherId,
            authorIds: book.authorIds ?? [],
          }}
          authors={lookups.authors}
          categories={lookups.categories}
          publishers={lookups.publishers}
          submitLabel="Save changes"
          loading={saving}
          onSubmit={handleSubmit}
          onCancel={() => navigate('/admin/books')}
        />
      </SectionCard>
    </div>
  );
}
