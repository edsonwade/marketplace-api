import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import apiClient from '../../api/client/apiClient';
import type { Stock, CreateStockRequest } from '../../api/types';

const STOCK_ENDPOINTS = {
  LIST: '/api/v1/stocks',
  GET: (id: number) => `/api/v1/stocks/${id}`,
  GET_BY_PRODUCT: (productId: number) => `/api/v1/stocks/product/${productId}`,
  CREATE: '/api/v1/stocks',
  UPDATE: (productId: number) => `/api/v1/stocks/product/${productId}`,
  DELETE: (id: number) => `/api/v1/stocks/${id}`,
};

export const stockService = {
  getAll: async (): Promise<Stock[]> => {
    const response = await apiClient.get<Stock[]>(STOCK_ENDPOINTS.LIST);
    return response.data;
  },

  getById: async (id: number): Promise<Stock> => {
    const response = await apiClient.get<Stock>(STOCK_ENDPOINTS.GET(id));
    return response.data;
  },

  getByProductId: async (productId: number): Promise<Stock> => {
    const response = await apiClient.get<Stock>(STOCK_ENDPOINTS.GET_BY_PRODUCT(productId));
    return response.data;
  },

  create: async (data: CreateStockRequest): Promise<Stock> => {
    const response = await apiClient.post<Stock>(STOCK_ENDPOINTS.CREATE, data);
    return response.data;
  },

  updateQuantity: async (productId: number, quantity: number): Promise<void> => {
    await apiClient.patch<void>(STOCK_ENDPOINTS.UPDATE(productId), null, {
      params: { quantity },
    });
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(STOCK_ENDPOINTS.DELETE(id));
  },
};

export const useStocks = () => {
  return useQuery({
    queryKey: ['stocks'],
    queryFn: stockService.getAll,
    staleTime: 1000 * 60 * 2,
  });
};

export const useStock = (id: number) => {
  return useQuery({
    queryKey: ['stock', id],
    queryFn: () => stockService.getById(id),
    enabled: !!id,
  });
};

export const useStockByProduct = (productId: number) => {
  return useQuery({
    queryKey: ['stock', 'product', productId],
    queryFn: () => stockService.getByProductId(productId),
    enabled: !!productId,
  });
};

export const useCreateStock = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateStockRequest) => stockService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['stocks'] });
    },
  });
};

export const useUpdateStockQuantity = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ productId, quantity }: { productId: number; quantity: number }) =>
      stockService.updateQuantity(productId, quantity),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['stocks'] });
      queryClient.invalidateQueries({ queryKey: ['stock', 'product', variables.productId] });
    },
  });
};

export const useDeleteStock = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => stockService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['stocks'] });
    },
  });
};
