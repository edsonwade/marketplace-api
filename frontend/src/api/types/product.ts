export interface Product {
  productId: number;
  name: string;
  quantity: number;
  price?: number;
  version: number;
}

export interface CreateProductRequest {
  name: string;
  quantity: number;
  price?: number;
}

export interface UpdateProductRequest {
  name: string;
  quantity: number;
  price?: number;
}
