import { apiClient } from '../api/client';
import type { Category } from '../types/book';

export interface CategoryRequest {
  name: string;
  description?: string;
}

export const categoryService = {
  getAll(): Promise<Category[]> {
    return apiClient<Category[]>('/api/categories/');
  },

  create(payload: CategoryRequest): Promise<Category> {
    return apiClient<Category>('/api/categories/create', { method: 'POST', body: payload });
  },

  update(id: string, payload: CategoryRequest): Promise<Category> {
    return apiClient<Category>(`/api/categories/update/${id}`, { method: 'PUT', body: payload });
  },

  remove(id: string): Promise<string> {
    return apiClient<string>(`/api/categories/${id}`, { method: 'DELETE' });
  },
};
