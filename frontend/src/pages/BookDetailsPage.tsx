import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { AlertBanner } from '../components/books/AlertBanner';
import { ConfirmDialog, LoadingBlock, SectionCard } from '../components/books/ConfirmDialog';
import { PageHeader } from '../components/ui/PageHeader';
import {
  resolveAuthorNames,
  resolveNameById,
  useBook,
  useBookLookups,
} from '../hooks/useBooks';
import { bookService } from '../services/bookService';
import { ApiError } from '../types/api';

function formatPrice(price: number): string {
  return new Intl.NumberFormat(undefined, {
    style: 'currency',
    currency: 'USD',
  }).format(price);
}

function formatDate(value: string | null): string {
  if (!value) return '—';
  return new Intl.DateTimeFormat(undefined, {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  }).format(new Date(`${value}T00:00:00`));
}

export function BookDetailsPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { book, loading, error } = useBook(id);
  const lookups = useBookLookups();
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);

  async function handleDelete() {
    if (!id) return;
    setDeleting(true);
    setActionError(null);
    try {
      await bookService.remove(id);
      navigate('/books', { replace: true });
    } catch (err) {
      setActionError(err instanceof ApiError ? err.message : 'Unable to delete book.');
      setConfirmOpen(false);
    } finally {
      setDeleting(false);
    }
  }

  if (loading || lookups.loading) {
    return <LoadingBlock label="Loading book" />;
  }

  if (error || !book) {
    return (
      <AlertBanner variant="error" title="Book not found">
        {error || 'This book could not be loaded.'}
      </AlertBanner>
    );
  }

  return (
    <div className="page-stack">
      <PageHeader
        backTo="/books"
        backLabel="Books"
        title={book.title}
        subtitle={resolveAuthorNames(book.authorIds, lookups.authors)}
        actions={
          <>
            <Link to={`/books/${book.id}/edit`} className="btn btn--secondary">
              Edit
            </Link>
            <button type="button" className="btn btn--danger" onClick={() => setConfirmOpen(true)}>
              Delete
            </button>
          </>
        }
      />

      {actionError ? <AlertBanner variant="error">{actionError}</AlertBanner> : null}

      <SectionCard>
        <div className="md-chip-row mb-5">
          <span className="md-chip">{resolveNameById(book.categoryId, lookups.categories)}</span>
          <span className="md-chip">{resolveNameById(book.publisherId, lookups.publishers)}</span>
          <span className="md-chip">{book.language}</span>
        </div>

        <dl className="md-detail-grid">
          <div>
            <dt>ISBN</dt>
            <dd>{book.isbn}</dd>
          </div>
          <div>
            <dt>Price</dt>
            <dd>{formatPrice(Number(book.price))}</dd>
          </div>
          <div>
            <dt>Published date</dt>
            <dd>{formatDate(book.publishedDate)}</dd>
          </div>
          <div>
            <dt>Language</dt>
            <dd>{book.language}</dd>
          </div>
          <div className="sm:col-span-2" style={{ gridColumn: '1 / -1' }}>
            <dt>Description</dt>
            <dd className="whitespace-pre-wrap">
              {book.description?.trim() || 'No description provided.'}
            </dd>
          </div>
        </dl>
      </SectionCard>

      <ConfirmDialog
        open={confirmOpen}
        title="Delete book?"
        message={`Delete “${book.title}”? This cannot be undone.`}
        loading={deleting}
        onCancel={() => setConfirmOpen(false)}
        onConfirm={() => void handleDelete()}
      />
    </div>
  );
}
