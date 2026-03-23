import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import apiClient from '../../api/client/apiClient';
import type { CartDto } from '../../api/types';

const CART_ENDPOINTS = {
  GET_BY_CUSTOMER:    (id: number) => `/api/v1/carts/customer/${id}`,
  CREATE:             `/api/v1/carts`,
  ADD_ITEM:           `/api/v1/carts/items`,
  UPDATE_ITEM:        (productId: number) => `/api/v1/carts/items/${productId}`,
  REMOVE_ITEM:        (productId: number) => `/api/v1/carts/items/${productId}`,
  CLEAR:              `/api/v1/carts/clear`,
  CHECKOUT_ORDER:     `/api/v1/carts/checkout-order`,
};

export const cartApiService = {
  getByCustomer: async (customerId: number): Promise<CartDto> => {
    const res = await apiClient.get<CartDto>(CART_ENDPOINTS.GET_BY_CUSTOMER(customerId));
    return res.data;
  },

  createCart: async (customerId: number): Promise<CartDto> => {
    const res = await apiClient.post<CartDto>(CART_ENDPOINTS.CREATE, null, { params: { customerId } });
    return res.data;
  },

  addItem: async (customerId: number, item: { productId: number; quantity: number }): Promise<CartDto> => {
    const res = await apiClient.post<CartDto>(CART_ENDPOINTS.ADD_ITEM, item, { params: { customerId } });
    return res.data;
  },

  updateItem: async (customerId: number, productId: number, quantity: number): Promise<CartDto> => {
    const res = await apiClient.put<CartDto>(CART_ENDPOINTS.UPDATE_ITEM(productId), null, {
      params: { customerId, quantity },
    });
    return res.data;
  },

  removeItem: async (customerId: number, productId: number): Promise<CartDto> => {
    const res = await apiClient.delete<CartDto>(CART_ENDPOINTS.REMOVE_ITEM(productId), {
      params: { customerId },
    });
    return res.data;
  },

  clearCart: async (customerId: number): Promise<CartDto> => {
    const res = await apiClient.delete<CartDto>(CART_ENDPOINTS.CLEAR, { params: { customerId } });
    return res.data;
  },

  checkoutAndCreateOrder: async (customerId: number) => {
    const res = await apiClient.post(CART_ENDPOINTS.CHECKOUT_ORDER, null, { params: { customerId } });
    return res.data; // OrderDto with orderId + totalAmount
  },
};

// ── Hooks ────────────────────────────────────────────────────────────────────

export const useBackendCart = (customerId: number | undefined) => {
  return useQuery({
    queryKey: ['cart', 'customer', customerId],
    queryFn: () => cartApiService.getByCustomer(customerId!),
    enabled: !!customerId,
    retry: false, // 404 = no active cart yet, don't retry
  });
};

export const useCreateCart = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (customerId: number) => cartApiService.createCart(customerId),
    onSuccess: (_, customerId) => qc.invalidateQueries({ queryKey: ['cart', 'customer', customerId] }),
  });
};

export const useAddItemToCart = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ customerId, productId, quantity }: { customerId: number; productId: number; quantity: number }) =>
      cartApiService.addItem(customerId, { productId, quantity }),
    onSuccess: (_, vars) => qc.invalidateQueries({ queryKey: ['cart', 'customer', vars.customerId] }),
  });
};

export const useUpdateCartItem = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ customerId, productId, quantity }: { customerId: number; productId: number; quantity: number }) =>
      cartApiService.updateItem(customerId, productId, quantity),
    onSuccess: (_, vars) => qc.invalidateQueries({ queryKey: ['cart', 'customer', vars.customerId] }),
  });
};

export const useRemoveCartItem = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ customerId, productId }: { customerId: number; productId: number }) =>
      cartApiService.removeItem(customerId, productId),
    onSuccess: (_, vars) => qc.invalidateQueries({ queryKey: ['cart', 'customer', vars.customerId] }),
  });
};

export const useClearCart = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (customerId: number) => cartApiService.clearCart(customerId),
    onSuccess: (_, customerId) => qc.invalidateQueries({ queryKey: ['cart', 'customer', customerId] }),
  });
};

// Returns the total number of items in the active backend cart — used by Header badge
export const useBackendCartCount = (customerId: number | undefined): number => {
  const { data: cart } = useBackendCart(customerId);
  if (!cart?.items) return 0;
  return cart.items.reduce((sum, item) => sum + (item.quantity ?? 0), 0);
};

export const useCheckoutAndCreateOrder = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (customerId: number) => cartApiService.checkoutAndCreateOrder(customerId),
    onSuccess: (_, customerId) => {
      qc.invalidateQueries({ queryKey: ['cart', 'customer', customerId] });
      // Invalidate all orders queries — both admin global and user-specific
      qc.invalidateQueries({ queryKey: ['orders'] });
      qc.invalidateQueries({ queryKey: ['orders', 'customer', customerId] });
      qc.invalidateQueries({ queryKey: ['orders', 'customer', customerId, 'unpaid'] });
    },
  });
};
