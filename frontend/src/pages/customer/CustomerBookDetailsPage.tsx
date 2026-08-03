import { Link, useParams } from 'react-router-dom';
import { AlertBanner } from '../../components/books/AlertBanner';
import { LoadingBlock, SectionCard } from '../../components/books/ConfirmDialog';
import { PageHeader } from '../../components/ui/PageHeader';
import {
  resolveAuthorNames,
  resolveNameById,
  useBook,
  useBookLookups,
} from '../../hooks/useBooks';

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

export function CustomerBookDetailsPage() {
  const { id } = useParams<{ id: string }>();
  const { book, loading, error } = useBook(id);
  const lookups = useBookLookups();

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
        backLabel="Browse books"
        title={book.title}
        subtitle={resolveAuthorNames(book.authorIds, lookups.authors)}
        actions={
          <Link to="/cart" className="btn btn--primary">
            Add to cart
          </Link>
        }
      />

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
            <dt>Published</dt>
            <dd>{formatDate(book.publishedDate)}</dd>
          </div>
          <div style={{ gridColumn: '1 / -1' }}>
            <dt>Description</dt>
            <dd className="whitespace-pre-wrap">
              {book.description?.trim() || 'No description provided.'}
            </dd>
          </div>
        </dl>
      </SectionCard>
    </div>
  );
}
