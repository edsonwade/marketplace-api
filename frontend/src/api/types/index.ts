export * from './auth';
export * from './customer';
export * from './product';
export * from './cart';
export * from './order';
export * from './payment';
export * from './discount';
export * from './stock';

export interface ApiError {
  message: string;
  status: number;
  timestamp: string;
  path?: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
