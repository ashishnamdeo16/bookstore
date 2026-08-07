import { apiClient } from '../api/client';
import { ApiError, type ApiErrorBody } from '../types/api';
import type { Book, BookCreateRequest } from '../types/book';
import { tokenStorage } from '../auth/tokenStorage';

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL !== undefined
    ? import.meta.env.VITE_API_BASE_URL
    : 'http://localhost:8080';

async function parseUploadError(response: Response): Promise<ApiError> {
  let message = 'Unable to upload cover image.';
  try {
    const data = (await response.json()) as ApiErrorBody;
    if (data.message) message = data.message;
  } catch {
    // Non-JSON body
  }
  return new ApiError(response.status, message);
}

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

  async uploadCover(id: string, file: File): Promise<Book> {
    const formData = new FormData();
    formData.append('file', file);

    const headers: Record<string, string> = {
      Accept: 'application/json',
    };
    const accessToken = tokenStorage.getAccessToken();
    if (accessToken) {
      headers.Authorization = `Bearer ${accessToken}`;
    }

    let response: Response;
    try {
      response = await fetch(`${API_BASE_URL}/api/books/${id}/cover`, {
        method: 'POST',
        headers,
        body: formData,
      });
    } catch {
      throw new ApiError(0, 'Unable to reach the API. Check your connection.');
    }

    if (!response.ok) {
      throw await parseUploadError(response);
    }

    return (await response.json()) as Book;
  },
};
