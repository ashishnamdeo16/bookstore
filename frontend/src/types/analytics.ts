export interface DashboardResponse {
  totalOrders: number;
  paidOrders: number;
  failedPayments: number;
  totalRevenue: number;
  averageOrderValue: number;
  booksSold: number;
  paymentSuccessRate: number;
}

export interface DailyRevenueItem {
  date: string;
  revenue: number;
  paidOrders: number;
}

export interface MonthlyRevenueItem {
  month: string;
  revenue: number;
  paidOrders: number;
  ordersCreated: number;
}

export interface DailyOrderItem {
  date: string;
  ordersCreated: number;
  paidOrders: number;
}

export interface TopBookItem {
  bookId: string;
  bookTitle: string;
  quantitySold: number;
  revenue: number;
}

export interface BooksAnalyticsResponse {
  booksSold: number;
  topBooks: TopBookItem[];
}

export interface PaymentsAnalyticsResponse {
  paidOrders: number;
  failedPayments: number;
  paymentSuccessRate: number;
}

export interface OrdersAnalyticsResponse {
  totalOrders: number;
  paidOrders: number;
  daily: DailyOrderItem[];
  monthly: MonthlyRevenueItem[];
}

export interface RevenueResponse {
  totalRevenue: number;
  daily: DailyRevenueItem[];
  monthly: MonthlyRevenueItem[];
}
