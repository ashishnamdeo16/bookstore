export type OrderStatus =
  | 'PENDING'
  | 'CREATED'
  | 'PAYMENT_PENDING'
  | 'PAYMENT_FAILED'
  | 'PAID'
  | 'CONFIRMED'
  | 'PROCESSING'
  | 'PACKED'
  | 'SHIPPED'
  | 'OUT_FOR_DELIVERY'
  | 'DELIVERED'
  | 'CANCEL_REQUESTED'
  | 'CANCELLED'
  | 'RETURN_REQUESTED'
  | 'RETURNED'
  | 'REFUNDED';

export interface OrderItemRequest {
  bookId: string;
  quantity: number;
}

export interface OrderRequest {
  items: OrderItemRequest[];
}

export interface OrderItemResponse {
  bookId: string;
  bookTitle: string;
  quantity: number;
  price: number;
}

export interface OrderResponse {
  orderId: string;
  userId: string;
  totalAmount: number;
  status: OrderStatus;
  items: OrderItemResponse[];
  createdAt?: string;
  updatedAt?: string;
}

export interface CartItem {
  bookId: string;
  title: string;
  price: number;
  quantity: number;
}
