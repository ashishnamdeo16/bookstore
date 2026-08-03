import { apiClient } from '../api/client';
import type { Publisher } from '../types/book';

export interface PublisherRequest {
  name: string;
  address?: string;
}

export const publisherService = {
  getAll(): Promise<Publisher[]> {
    return apiClient<Publisher[]>('/api/publishers/');
  },

  create(payload: PublisherRequest): Promise<Publisher> {
    return apiClient<Publisher>('/api/publishers/create', { method: 'POST', body: payload });
  },

  update(id: string, payload: PublisherRequest): Promise<Publisher> {
    return apiClient<Publisher>(`/api/publishers/update/${id}`, {
      method: 'PUT',
      body: payload,
    });
  },

  remove(id: string): Promise<string> {
    return apiClient<string>(`/api/publishers/${id}`, { method: 'DELETE' });
  },
};
