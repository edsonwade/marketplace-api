import type { Customer, Product } from './index';

export interface OrderItem {
  orderItemId: number;
  product: Product;
  quantity: number;
}

export interface Order {
  orderId: number;
  localDateTime: string;
  customer: Customer;
  orderItems: OrderItem[];
}

export interface CreateOrderRequest {
  customerId: number;
}

export interface UpdateOrderRequest {
  status?: string;
}
