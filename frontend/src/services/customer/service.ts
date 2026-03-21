import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import apiClient from '../../api/client/apiClient';
import type { Customer, CreateCustomerRequest, UpdateCustomerRequest } from '../../api/types';
import { decodeToken } from '../../utils/jwt';
import { cookies } from '../../utils/cookies';
import { useAuthStore } from '../../store';

const CUSTOMER_ENDPOINTS = {
  LIST: '/api/v1/customers',
  GET: (id: number) => `/api/v1/customers/${id}`,
  CREATE: '/api/v1/customers',
  UPDATE: (id: number) => `/api/v1/customers/${id}`,
  DELETE: (id: number) => `/api/v1/customers/${id}`,
};

export const customerService = {
  getAll: async (): Promise<Customer[]> => {
    const response = await apiClient.get<Customer[]>(CUSTOMER_ENDPOINTS.LIST);
    return response.data;
  },

  getById: async (id: number): Promise<Customer> => {
    const response = await apiClient.get<Customer>(CUSTOMER_ENDPOINTS.GET(id));
    return response.data;
  },

  create: async (data: CreateCustomerRequest): Promise<Customer> => {
    const response = await apiClient.post<Customer>(CUSTOMER_ENDPOINTS.CREATE, data);
    return response.data;
  },

  update: async (id: number, data: UpdateCustomerRequest): Promise<Customer> => {
    const response = await apiClient.put<Customer>(CUSTOMER_ENDPOINTS.UPDATE(id), data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(CUSTOMER_ENDPOINTS.DELETE(id));
  },
};

export const useCustomers = () => {
  return useQuery({
    queryKey: ['customers'],
    queryFn: customerService.getAll,
    staleTime: 1000 * 60 * 5,
  });
};

export const useCustomer = (id: number) => {
  return useQuery({
    queryKey: ['customer', id],
    queryFn: () => customerService.getById(id),
    enabled: !!id,
  });
};

export const useCreateCustomer = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateCustomerRequest) => customerService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['customers'] });
    },
  });
};

export const useUpdateCustomer = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateCustomerRequest }) =>
      customerService.update(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['customers'] });
      queryClient.invalidateQueries({ queryKey: ['customer', variables.id] });
    },
  });
};

export const useDeleteCustomer = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => customerService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['customers'] });
    },
  });
};

export const useCurrentUser = () => {
  const { setUser } = useAuthStore();
  const refreshToken = cookies.get('refresh_token');
  const customerId = refreshToken ? parseInt(decodeToken(refreshToken)?.sub || '0') : 0;
  const { data, isLoading, error } = useQuery({
    queryKey: ['customer', 'current'],
    queryFn: async () => {
      const customer = await customerService.getById(customerId);
      setUser(customer);
      return customer;
    },
    enabled: !!customerId,
    staleTime: 1000 * 60 * 5,
  });
  return { data, isLoading, error };
};
