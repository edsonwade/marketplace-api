import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import apiClient from '../../api/client/apiClient';
import type { OrderItem } from '../../api/types';

const ORDER_ITEM_ENDPOINTS = {
  LIST: '/api/orderItems',
  GET: (id: number) => `/api/orderItems/${id}`,
  CREATE: '/api/orderItems',
  DELETE: (id: number) => `/api/orderItems/${id}`,
};

export const orderItemService = {
  getAll: async (): Promise<OrderItem[]> => {
    const response = await apiClient.get<OrderItem[]>(ORDER_ITEM_ENDPOINTS.LIST);
    return response.data;
  },

  getById: async (id: number): Promise<OrderItem> => {
    const response = await apiClient.get<OrderItem>(ORDER_ITEM_ENDPOINTS.GET(id));
    return response.data;
  },

  create: async (data: Partial<OrderItem>): Promise<OrderItem> => {
    const response = await apiClient.post<OrderItem>(ORDER_ITEM_ENDPOINTS.CREATE, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(ORDER_ITEM_ENDPOINTS.DELETE(id));
  },
};

export const useOrderItems = () => {
  return useQuery({
    queryKey: ['orderItems'],
    queryFn: orderItemService.getAll,
    staleTime: 1000 * 60 * 5,
  });
};

export const useOrderItem = (id: number) => {
  return useQuery({
    queryKey: ['orderItem', id],
    queryFn: () => orderItemService.getById(id),
    enabled: !!id,
  });
};

export const useCreateOrderItem = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<OrderItem>) => orderItemService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orderItems'] });
    },
  });
};

export const useDeleteOrderItem = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => orderItemService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orderItems'] });
    },
  });
};
