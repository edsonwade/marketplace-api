export interface CartItem {
  id?: number;
  productId: number;
  productName: string;
  quantity: number;
  price: number;
  subtotal?: number;
  cartId?: number;
  createdAt?: string;
  updatedAt?: string;
  version?: number;
}

export interface Cart {
  id: number;
  customerId: number;
  status: string;
  items: CartItem[];
  totalAmount: number;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface AddToCartRequest {
  productId: number;
  quantity: number;
  price: number;
}

export interface UpdateCartItemRequest {
  quantity: number;
}
