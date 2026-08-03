import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { AlertBanner } from '../../components/books/AlertBanner';
import { LoadingBlock } from '../../components/books/ConfirmDialog';
import { EmptyState } from '../../components/ui/EmptyState';
import { PageHeader } from '../../components/ui/PageHeader';
import {
  resolveAuthorNames,
  resolveNameById,
  useBookLookups,
  useBooks,
} from '../../hooks/useBooks';

function formatPrice(price: number): string {
  return new Intl.NumberFormat(undefined, {
    style: 'currency',
    currency: 'USD',
  }).format(price);
}

export function BrowseBooksPage() {
  const { books, loading, error, refresh } = useBooks();
  const lookups = useBookLookups();
  const [query, setQuery] = useState('');

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return books;
    return books.filter((book) => {
      const authors = resolveAuthorNames(book.authorIds, lookups.authors).toLowerCase();
      return (
        book.title.toLowerCase().includes(q) ||
        book.language.toLowerCase().includes(q) ||
        authors.includes(q)
      );
    });
  }, [books, query, lookups.authors]);

  if (loading || lookups.loading) {
    return <LoadingBlock label="Loading books" />;
  }

  if (error || lookups.error) {
    return (
      <div className="grid gap-4">
        <AlertBanner variant="error" title="Couldn’t load books">
          {error || lookups.error}
        </AlertBanner>
        <button type="button" className="btn btn--secondary w-fit" onClick={() => void refresh()}>
          Try again
        </button>
      </div>
    );
  }

  return (
    <div className="page-stack">
      <PageHeader
        title="Browse books"
        subtitle={`${filtered.length} titles available`}
      />

      <label className="md-search max-w-xl">
        <span className="md-search__icon" aria-hidden="true">
          ⌕
        </span>
        <input
          type="search"
          placeholder="Search by title, author, or language"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          aria-label="Search books"
        />
      </label>

      {filtered.length === 0 ? (
        <EmptyState title="No books found" description="Try another search term." />
      ) : (
        <div className="book-grid">
          {filtered.map((book) => (
            <article key={book.id} className="book-card">
              <Link to={`/books/${book.id}`} className="book-card__title">
                {book.title}
              </Link>
              <p className="book-card__meta">
                {resolveAuthorNames(book.authorIds, lookups.authors)}
              </p>
              <p className="book-card__meta">
                {resolveNameById(book.categoryId, lookups.categories)} · {book.language}
              </p>
              <p className="book-card__price">{formatPrice(Number(book.price))}</p>
              <Link to={`/books/${book.id}`} className="btn btn--secondary w-fit">
                View details
              </Link>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
