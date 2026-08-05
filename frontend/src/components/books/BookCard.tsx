import { Link } from 'react-router-dom';
import type { Book } from '../../types/book';

function formatPrice(price: number): string {
  return new Intl.NumberFormat(undefined, {
    style: 'currency',
    currency: 'USD',
  }).format(price);
}

function coverTone(seed: string): number {
  let hash = 0;
  for (let i = 0; i < seed.length; i += 1) {
    hash = (hash + seed.charCodeAt(i) * (i + 1)) % 5;
  }
  return hash;
}

interface BookCardProps {
  book: Book;
  authors: string;
  category: string;
}

export function BookCard({ book, authors, category }: BookCardProps) {
  const initial = book.title.trim().charAt(0).toUpperCase() || 'B';
  const tone = coverTone(book.id || book.title);

  return (
    <article className="book-card">
      <Link to={`/books/${book.id}`} className="book-card__link">
        <div className={`book-card__cover book-card__cover--${tone}`} aria-hidden="true">
          <span className="book-card__initial">{initial}</span>
        </div>
        <div className="book-card__body">
          <h2 className="book-card__title">{book.title}</h2>
          <p className="book-card__meta">{authors}</p>
          <p className="book-card__meta">
            {category} · {book.language}
          </p>
          <div className="book-card__footer">
            <p className="book-card__price">{formatPrice(Number(book.price))}</p>
            <span className="book-card__cta">View</span>
          </div>
        </div>
      </Link>
    </article>
  );
}
