import { apiClient } from './client';
import type { UpdateProfileRequest, UserProfile } from '../types/user';

export const userService = {
  getProfile(userId: string): Promise<UserProfile> {
    return apiClient<UserProfile>(`/api/user/${userId}`);
  },

  updateProfile(userId: string, payload: UpdateProfileRequest): Promise<UserProfile> {
    return apiClient<UserProfile>(`/api/user/update/${userId}`, {
      method: 'PUT',
      body: payload,
    });
  },

  getAll(): Promise<UserProfile[]> {
    return apiClient<UserProfile[]>('/api/user/');
  },

  remove(userId: string): Promise<string> {
    return apiClient<string>(`/api/user/${userId}`, { method: 'DELETE' });
  },
};
