export type PaymentStatus = 'PAYMENT_PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';

export interface CheckoutPaymentRequest {
  checkoutId: string;
  items: Array<{
    bookId: string;
    quantity: number;
  }>;
}

export interface PaymentItemResponse {
  bookId: string;
  bookTitle: string;
  quantity: number;
  price: number;
}

export interface PaymentResponse {
  paymentId: string;
  checkoutId: string;
  orderId?: string | null;
  amount: number;
  status: PaymentStatus;
  paymentIntentId: string;
  clientSecret: string;
  items: PaymentItemResponse[];
}
