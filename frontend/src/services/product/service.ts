import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import apiClient from '../../api/client/apiClient';
import type { Product, CreateProductRequest, UpdateProductRequest } from '../../api/types';

const PRODUCT_ENDPOINTS = {
  LIST: '/api/v1/products',
  GET: (id: number) => `/api/v1/products/${id}`,
  CREATE: '/api/v1/products',
  UPDATE: (id: number) => `/api/v1/products/${id}`,
  DELETE: (id: number) => `/api/v1/products/${id}`,
};

export const productService = {
  getAll: async (): Promise<Product[]> => {
    const response = await apiClient.get<Product[]>(PRODUCT_ENDPOINTS.LIST);
    return response.data;
  },

  getById: async (id: number): Promise<Product> => {
    const response = await apiClient.get<Product>(PRODUCT_ENDPOINTS.GET(id));
    return response.data;
  },

  create: async (data: CreateProductRequest): Promise<Product> => {
    const response = await apiClient.post<Product>(PRODUCT_ENDPOINTS.CREATE, data);
    return response.data;
  },

  update: async (id: number, data: UpdateProductRequest, version: number): Promise<Product> => {
    const response = await apiClient.put<Product>(PRODUCT_ENDPOINTS.UPDATE(id), data, {
      headers: { 'If-Match': version },
    });
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(PRODUCT_ENDPOINTS.DELETE(id));
  },
};

export const useProducts = () => {
  return useQuery({
    queryKey: ['products'],
    queryFn: productService.getAll,
    staleTime: 1000 * 60 * 5,
  });
};

export const useProduct = (id: number) => {
  return useQuery({
    queryKey: ['product', id],
    queryFn: () => productService.getById(id),
    enabled: !!id,
  });
};

export const useCreateProduct = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateProductRequest) => productService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
    },
  });
};

export const useUpdateProduct = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data, version }: { id: number; data: UpdateProductRequest; version: number }) =>
      productService.update(id, data, version),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
      queryClient.invalidateQueries({ queryKey: ['product', variables.id] });
    },
  });
};

export const useDeleteProduct = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => productService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
    },
  });
};
