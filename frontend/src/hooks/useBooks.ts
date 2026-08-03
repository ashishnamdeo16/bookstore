import { useCallback, useEffect, useState } from 'react';
import { ApiError } from '../types/api';
import { authorService } from '../services/authorService';
import { bookService } from '../services/bookService';
import { categoryService } from '../services/categoryService';
import { publisherService } from '../services/publisherService';
import type { Author, Book, Category, Publisher } from '../types/book';

export function useBooks() {
  const [books, setBooks] = useState<Book[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setBooks(await bookService.getAll());
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Unable to load books.');
      setBooks([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  return { books, loading, error, refresh, setBooks };
}

export function useBook(id: string | undefined) {
  const [book, setBook] = useState<Book | null>(null);
  const [loading, setLoading] = useState(Boolean(id));
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    if (!id) {
      setBook(null);
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      setBook(await bookService.getById(id));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Unable to load book.');
      setBook(null);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  return { book, loading, error, refresh };
}

export function useBookLookups() {
  const [authors, setAuthors] = useState<Author[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [publishers, setPublishers] = useState<Publisher[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    void (async () => {
      setLoading(true);
      setError(null);
      try {
        const [authorList, categoryList, publisherList] = await Promise.all([
          authorService.getAll(),
          categoryService.getAll(),
          publisherService.getAll(),
        ]);
        if (!active) return;
        setAuthors(authorList);
        setCategories(categoryList);
        setPublishers(publisherList);
      } catch (err) {
        if (!active) return;
        setError(err instanceof ApiError ? err.message : 'Unable to load form options.');
      } finally {
        if (active) setLoading(false);
      }
    })();
    return () => {
      active = false;
    };
  }, []);

  return { authors, categories, publishers, loading, error };
}

export function authorName(author: Author): string {
  return `${author.firstName} ${author.lastName}`.trim();
}

export function resolveAuthorNames(authorIds: string[], authors: Author[]): string {
  if (!authorIds?.length) return '—';
  const names = authorIds
    .map((id) => authors.find((author) => author.id === id))
    .filter(Boolean)
    .map((author) => authorName(author!));
  return names.length ? names.join(', ') : '—';
}

export function resolveNameById(
  id: string | null | undefined,
  items: Array<{ id: string; name: string }>,
): string {
  if (!id) return '—';
  return items.find((item) => item.id === id)?.name ?? '—';
}
