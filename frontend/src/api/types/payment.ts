export interface Payment {
  id: number;
  orderId: number;
  customerId: number;
  paymentMethod: string;
  paymentStatus: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  amount: number;
  currency: string;
  transactionId: string;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface PaymentMethod {
  id: number;
  customerId: number;
  methodType: string;
  provider?: string;
  isDefault: boolean;
  isActive: boolean;
  lastFourDigits: string;
  expiryMonth: string;
  expiryYear: string;
  createdAt?: string;
  updatedAt?: string;
  version?: number;
}

export interface ProcessPaymentRequest {
  orderId: number;
  customerId: number;
  paymentMethod: string;
  token?: string;
}

export interface CreatePaymentMethodRequest {
  customerId: number;
  methodType: string;
  provider?: string;
  lastFourDigits: string;
  expiryMonth: string;
  expiryYear: string;
  isDefault?: boolean;
}
