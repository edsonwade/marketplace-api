export interface Stock {
  stockId: number;
  productId: number;
  quantity: number;
  location: string;
}

export interface CreateStockRequest {
  productId: number;
  quantity: number;
  location: string;
}

export interface UpdateStockRequest {
  quantity: number;
}
