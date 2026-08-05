import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { AlertBanner } from '../../components/books/AlertBanner';
import { LoadingBlock } from '../../components/books/ConfirmDialog';
import { Button } from '../../components/ui/Button';
import { PageHeader } from '../../components/ui/PageHeader';
import { toast } from '../../components/ui/toast';
import { useCart } from '../../features/cart/CartContext';
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
  const { addItem } = useCart();
  const [added, setAdded] = useState(false);

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

  const authors = resolveAuthorNames(book.authorIds, lookups.authors);
  const category = resolveNameById(book.categoryId, lookups.categories);
  const publisher = resolveNameById(book.publisherId, lookups.publishers);
  const initial = book.title.trim().charAt(0).toUpperCase() || 'B';

  function handleAddToCart() {
    addItem({
      bookId: book!.id,
      title: book!.title,
      price: Number(book!.price),
    });
    setAdded(true);
    toast.success(`${book!.title} added to cart`);
    window.setTimeout(() => setAdded(false), 2000);
  }

  return (
    <div className="page-stack">
      <PageHeader backTo="/books" backLabel="Browse books" />

      <article className="book-detail">
        <div className="book-detail__cover" aria-hidden="true">
          <span className="book-detail__initial">{initial}</span>
        </div>

        <div className="book-detail__content">
          <div className="book-detail__header">
            <div className="md-chip-row">
              <span className="md-chip">{category}</span>
              <span className="md-chip">{book.language}</span>
            </div>
            <h1 className="book-detail__title">{book.title}</h1>
            <p className="book-detail__authors">{authors}</p>
          </div>

          <div className="book-detail__buy">
            <p className="book-detail__price">{formatPrice(Number(book.price))}</p>
            <div className="book-detail__actions">
              <Button variant="primary" size="lg" onClick={handleAddToCart}>
                {added ? 'Added to cart' : 'Add to cart'}
              </Button>
              <Link to="/cart" className="btn btn--secondary btn--lg">
                View cart
              </Link>
            </div>
          </div>

          <section className="book-detail__section">
            <h2 className="book-detail__section-title">About this book</h2>
            <p className="book-detail__description">
              {book.description?.trim() || 'No description provided.'}
            </p>
          </section>

          <section className="book-detail__section">
            <h2 className="book-detail__section-title">Details</h2>
            <dl className="book-detail__meta">
              <div>
                <dt>ISBN</dt>
                <dd>{book.isbn}</dd>
              </div>
              <div>
                <dt>Publisher</dt>
                <dd>{publisher}</dd>
              </div>
              <div>
                <dt>Published</dt>
                <dd>{formatDate(book.publishedDate)}</dd>
              </div>
              <div>
                <dt>Category</dt>
                <dd>{category}</dd>
              </div>
              <div>
                <dt>Language</dt>
                <dd>{book.language}</dd>
              </div>
            </dl>
          </section>
        </div>
      </article>
    </div>
  );
}
