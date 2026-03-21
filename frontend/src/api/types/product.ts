export interface Product {
  productId: number;
  name: string;
  quantity: number;
  version: number;
}

export interface CreateProductRequest {
  name: string;
  quantity: number;
}

export interface UpdateProductRequest {
  name: string;
  quantity: number;
}
