import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { AlertBanner } from '../../components/books/AlertBanner';
import { ConfirmDialog, LoadingBlock } from '../../components/books/ConfirmDialog';
import { EmptyState } from '../../components/ui/EmptyState';
import { PageHeader } from '../../components/ui/PageHeader';
import {
  resolveAuthorNames,
  resolveNameById,
  useBookLookups,
  useBooks,
} from '../../hooks/useBooks';
import { bookService } from '../../services/bookService';
import { ApiError } from '../../types/api';
import type { Book } from '../../types/book';

type SortKey = 'title' | 'price' | 'publishedDate' | 'language';
const PAGE_SIZE = 8;

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
    month: 'short',
    day: 'numeric',
  }).format(new Date(`${value}T00:00:00`));
}

function compareBooks(a: Book, b: Book, sortKey: SortKey): number {
  if (sortKey === 'price') return Number(a.price) - Number(b.price);
  if (sortKey === 'publishedDate') {
    return (a.publishedDate ?? '').localeCompare(b.publishedDate ?? '');
  }
  if (sortKey === 'language') return a.language.localeCompare(b.language);
  return a.title.localeCompare(b.title);
}

export function AdminManageBooksPage() {
  const { books, loading, error, refresh, setBooks } = useBooks();
  const lookups = useBookLookups();
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);
  const [query, setQuery] = useState('');
  const [sortKey, setSortKey] = useState<SortKey>('title');
  const [sortAsc, setSortAsc] = useState(true);
  const [page, setPage] = useState(1);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    let next = [...books];
    if (q) {
      next = next.filter((book) => {
        const authors = resolveAuthorNames(book.authorIds, lookups.authors).toLowerCase();
        return (
          book.title.toLowerCase().includes(q) ||
          book.isbn.toLowerCase().includes(q) ||
          authors.includes(q)
        );
      });
    }
    next.sort((a, b) => {
      const result = compareBooks(a, b, sortKey);
      return sortAsc ? result : -result;
    });
    return next;
  }, [books, query, sortKey, sortAsc, lookups.authors]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const currentPage = Math.min(page, totalPages);
  const pageItems = filtered.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE);

  async function handleDelete() {
    if (!deleteId) return;
    setDeleting(true);
    setActionError(null);
    try {
      await bookService.remove(deleteId);
      setBooks((current) => current.filter((book) => book.id !== deleteId));
      setDeleteId(null);
    } catch (err) {
      setActionError(err instanceof ApiError ? err.message : 'Unable to delete book.');
    } finally {
      setDeleting(false);
    }
  }

  if (loading || lookups.loading) return <LoadingBlock label="Loading books" />;

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
        title="Manage books"
        subtitle={`${filtered.length} titles`}
        actions={
          <Link to="/admin/books/new" className="btn btn--primary">
            Add book
          </Link>
        }
      />

      {actionError ? <AlertBanner variant="error">{actionError}</AlertBanner> : null}

      <div className="md-toolbar">
        <label className="md-search">
          <span className="md-search__icon" aria-hidden="true">
            ⌕
          </span>
          <input
            type="search"
            placeholder="Search books"
            value={query}
            onChange={(event) => {
              setQuery(event.target.value);
              setPage(1);
            }}
          />
        </label>
        <div className="flex flex-wrap gap-2">
          <select
            className="md-select"
            value={sortKey}
            onChange={(event) => setSortKey(event.target.value as SortKey)}
          >
            <option value="title">Sort by title</option>
            <option value="price">Sort by price</option>
            <option value="publishedDate">Sort by date</option>
            <option value="language">Sort by language</option>
          </select>
          <button type="button" className="btn btn--secondary" onClick={() => setSortAsc((v) => !v)}>
            {sortAsc ? 'A → Z' : 'Z → A'}
          </button>
        </div>
      </div>

      {books.length === 0 ? (
        <EmptyState
          title="No books yet"
          description="Add your first catalog title."
          actionLabel="Add book"
          actionTo="/admin/books/new"
        />
      ) : filtered.length === 0 ? (
        <EmptyState title="No matches" description="Try another search." />
      ) : (
        <>
          <div className="md-table-wrap">
            <table className="md-table">
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Category</th>
                  <th>Publisher</th>
                  <th>Price</th>
                  <th>Published</th>
                  <th aria-label="Actions" />
                </tr>
              </thead>
              <tbody>
                {pageItems.map((book) => (
                  <tr key={book.id}>
                    <td>
                      <div className="md-table__title">{book.title}</div>
                      <div className="md-table__meta">
                        {resolveAuthorNames(book.authorIds, lookups.authors)}
                      </div>
                    </td>
                    <td>{resolveNameById(book.categoryId, lookups.categories)}</td>
                    <td>{resolveNameById(book.publisherId, lookups.publishers)}</td>
                    <td>{formatPrice(Number(book.price))}</td>
                    <td>{formatDate(book.publishedDate)}</td>
                    <td>
                      <div className="md-table__actions">
                        <Link to={`/admin/books/${book.id}/edit`} className="md-icon-btn">
                          Edit
                        </Link>
                        <button
                          type="button"
                          className="md-icon-btn md-icon-btn--danger"
                          onClick={() => setDeleteId(book.id)}
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
          <div className="md-pagination">
            <span>
              Showing {(currentPage - 1) * PAGE_SIZE + 1}–
              {Math.min(currentPage * PAGE_SIZE, filtered.length)} of {filtered.length}
            </span>
            <div className="md-pagination__controls">
              <button
                type="button"
                className="btn btn--secondary"
                disabled={currentPage <= 1}
                onClick={() => setPage((v) => Math.max(1, v - 1))}
              >
                Previous
              </button>
              <span className="px-2">
                Page {currentPage} of {totalPages}
              </span>
              <button
                type="button"
                className="btn btn--secondary"
                disabled={currentPage >= totalPages}
                onClick={() => setPage((v) => Math.min(totalPages, v + 1))}
              >
                Next
              </button>
            </div>
          </div>
        </>
      )}

      <ConfirmDialog
        open={Boolean(deleteId)}
        title="Delete book?"
        message="This permanently removes the book from the catalog."
        loading={deleting}
        onCancel={() => setDeleteId(null)}
        onConfirm={() => void handleDelete()}
      />
    </div>
  );
}
