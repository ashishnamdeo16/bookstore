import { apiClient } from './client';
import type { CheckoutPaymentRequest, PaymentResponse } from '../types/payment';

export const paymentService = {
  createCheckout(payload: CheckoutPaymentRequest): Promise<PaymentResponse> {
    return apiClient<PaymentResponse>('/api/payments/checkout', {
      method: 'POST',
      body: payload,
    });
  },

  getById(paymentId: string): Promise<PaymentResponse> {
    return apiClient<PaymentResponse>(`/api/payments/${paymentId}`);
  },
};
