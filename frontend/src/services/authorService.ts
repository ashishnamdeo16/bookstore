import { apiClient } from '../api/client';
import type { Author } from '../types/book';

export interface AuthorRequest {
  firstName: string;
  lastName?: string;
  biography?: string;
  country?: string;
}

export const authorService = {
  getAll(): Promise<Author[]> {
    return apiClient<Author[]>('/api/authors/');
  },

  create(payload: AuthorRequest): Promise<Author> {
    return apiClient<Author>('/api/authors/create', { method: 'POST', body: payload });
  },

  update(id: string, payload: AuthorRequest): Promise<Author> {
    return apiClient<Author>(`/api/authors/update/${id}`, { method: 'PUT', body: payload });
  },

  remove(id: string): Promise<string> {
    return apiClient<string>(`/api/authors/${id}`, { method: 'DELETE' });
  },
};
