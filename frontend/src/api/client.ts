import { ApiError, type ApiErrorBody } from '../types/api';
import type { AuthTokens } from '../types/auth';
import { tokenStorage } from '../auth/tokenStorage';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE';

interface RequestOptions {
  method?: HttpMethod;
  body?: unknown;
  auth?: boolean;
  retry?: boolean;
}

type SessionClearedListener = () => void;

let refreshPromise: Promise<AuthTokens | null> | null = null;
let onSessionCleared: SessionClearedListener | null = null;

export function setSessionClearedListener(listener: SessionClearedListener | null): void {
  onSessionCleared = listener;
}

async function parseError(response: Response): Promise<ApiError> {
  let message = 'Something went wrong. Please try again.';
  let validationErrors: string[] = [];

  try {
    const data = (await response.json()) as ApiErrorBody;
    if (data.message) message = data.message;
    if (data.validationErrors?.length) validationErrors = data.validationErrors;
  } catch {
    // Non-JSON error body
  }

  if (response.status === 401) {
    message =
      dataMessageOrFallback(message, 'Your session has expired. Please sign in again.');
  }

  if (response.status === 403) {
    message = dataMessageOrFallback(
      message,
      'You do not have permission to access this resource.',
    );
  }

  return new ApiError(response.status, message, validationErrors);
}

function dataMessageOrFallback(message: string, fallback: string): string {
  return message && message !== 'Something went wrong. Please try again.'
    ? message
    : fallback;
}

async function refreshTokens(): Promise<AuthTokens | null> {
  const refreshToken = tokenStorage.getRefreshToken();
  if (!refreshToken) return null;

  const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  });

  if (!response.ok) {
    tokenStorage.clear();
    onSessionCleared?.();
    return null;
  }

  const tokens = (await response.json()) as AuthTokens;
  tokenStorage.setTokens(tokens);
  return tokens;
}

function refreshAccessToken(): Promise<AuthTokens | null> {
  if (!refreshPromise) {
    refreshPromise = refreshTokens().finally(() => {
      refreshPromise = null;
    });
  }
  return refreshPromise;
}

export async function apiClient<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, auth = true, retry = true } = options;

  const headers: Record<string, string> = {
    Accept: 'application/json',
  };

  if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
  }

  if (auth) {
    const accessToken = tokenStorage.getAccessToken();
    if (accessToken) {
      headers.Authorization = `Bearer ${accessToken}`;
    }
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (response.status === 401 && auth && retry) {
    const tokens = await refreshAccessToken();
    if (tokens) {
      return apiClient<T>(path, { ...options, retry: false });
    }
    throw new ApiError(401, 'Your session has expired. Please sign in again.');
  }

  if (!response.ok) {
    throw await parseError(response);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  if (!text) {
    return undefined as T;
  }

  try {
    return JSON.parse(text) as T;
  } catch {
    return text as T;
  }
}
