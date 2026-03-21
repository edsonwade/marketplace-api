import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import apiClient from '../../api/client/apiClient';
import type { Cart, AddToCartRequest } from '../../api/types';

const CART_ENDPOINTS = {
  LIST: '/api/v1/carts',
  GET: (id: number) => `/api/v1/carts/${id}`,
  GET_BY_CUSTOMER: (customerId: number) => `/api/v1/carts/customer/${customerId}`,
  CREATE: '/api/v1/carts',
  ADD_ITEM: '/api/v1/carts/items',
  UPDATE_ITEM: (productId: number) => `/api/v1/carts/items/${productId}`,
  REMOVE_ITEM: (productId: number) => `/api/v1/carts/items/${productId}`,
  CLEAR: '/api/v1/carts/clear',
  CHECKOUT: '/api/v1/carts/checkout',
  DELETE: (id: number) => `/api/v1/carts/${id}`,
};

export const cartService = {
  getAll: async (): Promise<Cart[]> => {
    const response = await apiClient.get<Cart[]>(CART_ENDPOINTS.LIST);
    return response.data;
  },

  getById: async (id: number): Promise<Cart> => {
    const response = await apiClient.get<Cart>(CART_ENDPOINTS.GET(id));
    return response.data;
  },

  getByCustomerId: async (customerId: number): Promise<Cart> => {
    const response = await apiClient.get<Cart>(CART_ENDPOINTS.GET_BY_CUSTOMER(customerId));
    return response.data;
  },

  create: async (customerId: number): Promise<Cart> => {
    const response = await apiClient.post<Cart>(CART_ENDPOINTS.CREATE, null, {
      params: { customerId },
    });
    return response.data;
  },

  addItem: async (customerId: number, data: AddToCartRequest): Promise<Cart> => {
    const response = await apiClient.post<Cart>(CART_ENDPOINTS.ADD_ITEM, data, {
      params: { customerId },
    });
    return response.data;
  },

  updateItemQuantity: async (customerId: number, productId: number, quantity: number): Promise<Cart> => {
    const response = await apiClient.put<Cart>(
      CART_ENDPOINTS.UPDATE_ITEM(productId),
      null,
      { params: { customerId, quantity } }
    );
    return response.data;
  },

  removeItem: async (customerId: number, productId: number): Promise<Cart> => {
    const response = await apiClient.delete<Cart>(CART_ENDPOINTS.REMOVE_ITEM(productId), {
      params: { customerId },
    });
    return response.data;
  },

  clear: async (customerId: number): Promise<Cart> => {
    const response = await apiClient.delete<Cart>(CART_ENDPOINTS.CLEAR, {
      params: { customerId },
    });
    return response.data;
  },

  checkout: async (customerId: number): Promise<Cart> => {
    const response = await apiClient.post<Cart>(CART_ENDPOINTS.CHECKOUT, null, {
      params: { customerId },
    });
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(CART_ENDPOINTS.DELETE(id));
  },
};

export const useCarts = () => {
  return useQuery({
    queryKey: ['carts'],
    queryFn: cartService.getAll,
    staleTime: 1000 * 60 * 2,
  });
};

export const useCart = (id: number) => {
  return useQuery({
    queryKey: ['cart', id],
    queryFn: () => cartService.getById(id),
    enabled: !!id,
  });
};

export const useCartByCustomer = (customerId: number) => {
  return useQuery({
    queryKey: ['cart', 'customer', customerId],
    queryFn: () => cartService.getByCustomerId(customerId),
    enabled: !!customerId,
  });
};

export const useCreateCart = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (customerId: number) => cartService.create(customerId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['carts'] });
    },
  });
};

export const useAddToCart = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ customerId, data }: { customerId: number; data: AddToCartRequest }) =>
      cartService.addItem(customerId, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['cart', 'customer', variables.customerId] });
    },
  });
};

export const useUpdateCartItem = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ customerId, productId, quantity }: { customerId: number; productId: number; quantity: number }) =>
      cartService.updateItemQuantity(customerId, productId, quantity),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['cart', 'customer', variables.customerId] });
    },
  });
};

export const useRemoveFromCart = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ customerId, productId }: { customerId: number; productId: number }) =>
      cartService.removeItem(customerId, productId),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['cart', 'customer', variables.customerId] });
    },
  });
};

export const useClearCart = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (customerId: number) => cartService.clear(customerId),
    onSuccess: (_, customerId) => {
      queryClient.invalidateQueries({ queryKey: ['cart', 'customer', customerId] });
    },
  });
};

export const useCheckout = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (customerId: number) => cartService.checkout(customerId),
    onSuccess: (_, customerId) => {
      queryClient.invalidateQueries({ queryKey: ['cart', 'customer', customerId] });
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
};

export const useDeleteCart = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => cartService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['carts'] });
    },
  });
};
