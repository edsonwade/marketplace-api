import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import apiClient from '../../api/client/apiClient';
import type { Order, CreateOrderRequest, UpdateOrderRequest } from '../../api/types';

const ORDER_ENDPOINTS = {
  LIST: '/api/v1/orders',
  GET: (id: number) => `/api/v1/orders/${id}`,
  CREATE: '/api/v1/orders',
  UPDATE: (id: number) => `/api/v1/orders/${id}`,
  DELETE: (id: number) => `/api/v1/orders/${id}`,
};

export const orderService = {
  getAll: async (): Promise<Order[]> => {
    const response = await apiClient.get<Order[]>(ORDER_ENDPOINTS.LIST);
    return response.data;
  },

  getById: async (id: number): Promise<Order> => {
    const response = await apiClient.get<Order>(ORDER_ENDPOINTS.GET(id));
    return response.data;
  },

  create: async (data: CreateOrderRequest): Promise<Order> => {
    const response = await apiClient.post<Order>(ORDER_ENDPOINTS.CREATE, data);
    return response.data;
  },

  update: async (id: number, data: UpdateOrderRequest): Promise<Order> => {
    const response = await apiClient.put<Order>(ORDER_ENDPOINTS.UPDATE(id), data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(ORDER_ENDPOINTS.DELETE(id));
  },
};

export const useOrders = () => {
  return useQuery({
    queryKey: ['orders'],
    queryFn: orderService.getAll,
    staleTime: 1000 * 60 * 5,
  });
};

export const useOrder = (id: number) => {
  return useQuery({
    queryKey: ['order', id],
    queryFn: () => orderService.getById(id),
    enabled: !!id,
  });
};

export const useCreateOrder = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateOrderRequest) => orderService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
};

export const useUpdateOrder = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateOrderRequest }) =>
      orderService.update(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      queryClient.invalidateQueries({ queryKey: ['order', variables.id] });
    },
  });
};

export const useDeleteOrder = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => orderService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
};
