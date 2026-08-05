import { apiClient } from './client';
import type { OrderResponse } from '../types/order';

export const orderService = {
  getMine(): Promise<OrderResponse[]> {
    return apiClient<OrderResponse[]>('/api/orders/me');
  },

  getById(orderId: string): Promise<OrderResponse> {
    return apiClient<OrderResponse>(`/api/orders/${orderId}`);
  },

  getByPaymentId(paymentId: string): Promise<OrderResponse> {
    return apiClient<OrderResponse>(`/api/orders/payment/${paymentId}`);
  },

  cancel(orderId: string): Promise<void> {
    return apiClient<void>(`/api/orders/${orderId}/cancel`, {
      method: 'POST',
    });
  },
};
