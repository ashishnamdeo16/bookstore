import { apiClient } from '../api/client';
import type { Book, BookCreateRequest } from '../types/book';

export const bookService = {
  getAll(): Promise<Book[]> {
    return apiClient<Book[]>('/api/books/');
  },

  getById(id: string): Promise<Book> {
    return apiClient<Book>(`/api/books/${id}`);
  },

  create(payload: BookCreateRequest): Promise<Book> {
    return apiClient<Book>('/api/books/create', {
      method: 'POST',
      body: payload,
    });
  },

  update(id: string, payload: BookCreateRequest): Promise<Book> {
    return apiClient<Book>(`/api/books/update/${id}`, {
      method: 'PUT',
      body: payload,
    });
  },

  remove(id: string): Promise<string> {
    return apiClient<string>(`/api/books/${id}`, {
      method: 'DELETE',
    });
  },
};
