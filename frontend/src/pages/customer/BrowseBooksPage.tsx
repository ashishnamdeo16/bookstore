import { useMemo, useState } from 'react';
import { AlertBanner } from '../../components/books/AlertBanner';
import { BookCard } from '../../components/books/BookCard';
import { LoadingBlock } from '../../components/books/ConfirmDialog';
import { Button } from '../../components/ui/Button';
import { EmptyState } from '../../components/ui/EmptyState';
import { PageHeader } from '../../components/ui/PageHeader';
import {
  resolveAuthorNames,
  resolveNameById,
  useBookLookups,
  useBooks,
} from '../../hooks/useBooks';

export function BrowseBooksPage() {
  const { books, loading, error, refresh } = useBooks();
  const lookups = useBookLookups();
  const [query, setQuery] = useState('');
  const [categoryId, setCategoryId] = useState('');

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    return books.filter((book) => {
      if (categoryId && book.categoryId !== categoryId) return false;
      if (!q) return true;
      const authors = resolveAuthorNames(book.authorIds, lookups.authors).toLowerCase();
      const category = resolveNameById(book.categoryId, lookups.categories).toLowerCase();
      return (
        book.title.toLowerCase().includes(q) ||
        book.language.toLowerCase().includes(q) ||
        authors.includes(q) ||
        category.includes(q) ||
        book.isbn.toLowerCase().includes(q)
      );
    });
  }, [books, query, categoryId, lookups.authors, lookups.categories]);

  const hasFilters = Boolean(query.trim() || categoryId);

  if (loading || lookups.loading) {
    return <LoadingBlock label="Loading books" />;
  }

  if (error || lookups.error) {
    return (
      <div className="page-stack">
        <AlertBanner variant="error" title="Couldn’t load books">
          {error || lookups.error}
        </AlertBanner>
        <Button variant="secondary" className="w-fit" onClick={() => void refresh()}>
          Try again
        </Button>
      </div>
    );
  }

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Catalog"
        title="Browse books"
        subtitle={
          hasFilters
            ? `${filtered.length} match${filtered.length === 1 ? '' : 'es'} of ${books.length}`
            : `${books.length} titles available`
        }
      />

      <div className="browse-toolbar">
        <label className="md-search browse-toolbar__search">
          <span className="md-search__icon" aria-hidden="true">
            ⌕
          </span>
          <input
            type="search"
            placeholder="Search title, author, category, ISBN…"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            aria-label="Search books"
          />
        </label>

        <label className="browse-toolbar__filter">
          <span className="sr-only">Category</span>
          <select
            className="md-select"
            value={categoryId}
            onChange={(event) => setCategoryId(event.target.value)}
            aria-label="Filter by category"
          >
            <option value="">All categories</option>
            {lookups.categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </label>

        {hasFilters ? (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => {
              setQuery('');
              setCategoryId('');
            }}
          >
            Clear
          </Button>
        ) : null}
      </div>

      {filtered.length === 0 ? (
        <EmptyState
          title="No books found"
          description={
            hasFilters
              ? 'Try another search term or clear filters.'
              : 'The catalog is empty right now.'
          }
          actionLabel={hasFilters ? 'Clear filters' : undefined}
          onAction={
            hasFilters
              ? () => {
                  setQuery('');
                  setCategoryId('');
                }
              : undefined
          }
        />
      ) : (
        <div className="book-grid">
          {filtered.map((book) => (
            <BookCard
              key={book.id}
              book={book}
              authors={resolveAuthorNames(book.authorIds, lookups.authors)}
              category={resolveNameById(book.categoryId, lookups.categories)}
            />
          ))}
        </div>
      )}
    </div>
  );
}
