import { apiClient } from './client';
import type {
  BooksAnalyticsResponse,
  DashboardResponse,
  OrdersAnalyticsResponse,
  PaymentsAnalyticsResponse,
  RevenueResponse,
} from '../types/analytics';

export const analyticsService = {
  getDashboard(): Promise<DashboardResponse> {
    return apiClient<DashboardResponse>('/analytics/dashboard');
  },

  getRevenue(): Promise<RevenueResponse> {
    return apiClient<RevenueResponse>('/analytics/revenue');
  },

  getOrders(): Promise<OrdersAnalyticsResponse> {
    return apiClient<OrdersAnalyticsResponse>('/analytics/orders');
  },

  getBooks(): Promise<BooksAnalyticsResponse> {
    return apiClient<BooksAnalyticsResponse>('/analytics/books');
  },

  getPayments(): Promise<PaymentsAnalyticsResponse> {
    return apiClient<PaymentsAnalyticsResponse>('/analytics/payments');
  },
};
