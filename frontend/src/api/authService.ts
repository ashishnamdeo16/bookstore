import { apiClient } from './client';
import { ApiError } from '../types/api';
import type {
  AuthTokens,
  LoginRequest,
  RegisterRequest,
  RegisterResponse,
} from '../types/auth';
import { getDeviceId } from '../auth/deviceId';
import { tokenStorage } from '../auth/tokenStorage';

export const authService = {
  async register(payload: RegisterRequest): Promise<RegisterResponse> {
    return apiClient<RegisterResponse>('/auth/register', {
      method: 'POST',
      body: payload,
      auth: false,
    });
  },

  async login(payload: Omit<LoginRequest, 'deviceId'>): Promise<AuthTokens> {
    const tokens = await apiClient<AuthTokens>('/auth/login', {
      method: 'POST',
      body: {
        ...payload,
        deviceId: getDeviceId(),
      },
      auth: false,
    });
    if (!tokens?.accessToken || !tokens?.refreshToken) {
      throw new ApiError(502, 'Sign-in succeeded but the server did not return tokens.');
    }
    tokenStorage.setTokens(tokens);
    return tokens;
  },

  async logout(): Promise<void> {
    const refreshToken = tokenStorage.getRefreshToken();
    try {
      if (refreshToken) {
        await apiClient<void>('/auth/logout', {
          method: 'POST',
          body: { refreshToken },
          auth: false,
          retry: false,
        });
      }
    } finally {
      // Clears auth tokens only — deviceId stays so the browser keeps its session identity.
      tokenStorage.clear();
    }
  },

  async logoutAll(): Promise<void> {
    try {
      await apiClient<void>('/auth/logout-all', {
        method: 'POST',
        retry: false,
      });
    } finally {
      tokenStorage.clear();
    }
  },
};
