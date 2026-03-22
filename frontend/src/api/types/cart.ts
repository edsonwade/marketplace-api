export interface CartItemDto {
  id?: number;
  cartItemId?: number;
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

export interface CartDto {
  id: number;
  cartId?: number;
  customerId: number;
  status: string;
  items: CartItemDto[];
  totalAmount: number;
  createdAt?: string;
  updatedAt?: string;
  version?: number;
}

// Legacy aliases kept for backwards compat
export type CartItem = CartItemDto;
export type Cart     = CartDto;

export interface AddToCartRequest {
  productId: number;
  quantity: number;
}

export interface UpdateCartItemRequest {
  quantity: number;
}
