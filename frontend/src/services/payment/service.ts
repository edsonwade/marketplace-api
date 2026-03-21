import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import apiClient from '../../api/client/apiClient';
import type { Payment, PaymentMethod, ProcessPaymentRequest, CreatePaymentMethodRequest } from '../../api/types';

const PAYMENT_ENDPOINTS = {
  LIST: '/api/v1/payments',
  GET: (id: number) => `/api/v1/payments/${id}`,
  GET_BY_ORDER: (orderId: number) => `/api/v1/payments/order/${orderId}`,
  GET_BY_CUSTOMER: (customerId: number) => `/api/v1/payments/customer/${customerId}`,
  CREATE: '/api/v1/payments',
  PROCESS: '/api/v1/payments/process',
  UPDATE_STATUS: (id: number) => `/api/v1/payments/${id}/status`,
  DELETE: (id: number) => `/api/v1/payments/${id}`,
  METHODS_BY_CUSTOMER: (customerId: number) => `/api/v1/payments/methods/customer/${customerId}`,
  METHOD_GET: (id: number) => `/api/v1/payments/methods/${id}`,
  METHOD_CREATE: '/api/v1/payments/methods',
  METHOD_SET_DEFAULT: (id: number) => `/api/v1/payments/methods/${id}/default`,
  METHOD_DELETE: (id: number) => `/api/v1/payments/methods/${id}`,
};

export const paymentService = {
  getAll: async (): Promise<Payment[]> => {
    const response = await apiClient.get<Payment[]>(PAYMENT_ENDPOINTS.LIST);
    return response.data;
  },

  getById: async (id: number): Promise<Payment> => {
    const response = await apiClient.get<Payment>(PAYMENT_ENDPOINTS.GET(id));
    return response.data;
  },

  getByOrderId: async (orderId: number): Promise<Payment[]> => {
    const response = await apiClient.get<Payment[]>(PAYMENT_ENDPOINTS.GET_BY_ORDER(orderId));
    return response.data;
  },

  getByCustomerId: async (customerId: number): Promise<Payment[]> => {
    const response = await apiClient.get<Payment[]>(PAYMENT_ENDPOINTS.GET_BY_CUSTOMER(customerId));
    return response.data;
  },

  create: async (data: Partial<Payment>): Promise<Payment> => {
    const response = await apiClient.post<Payment>(PAYMENT_ENDPOINTS.CREATE, data);
    return response.data;
  },

  process: async (data: ProcessPaymentRequest): Promise<Payment> => {
    const response = await apiClient.post<Payment>(PAYMENT_ENDPOINTS.PROCESS, null, {
      params: {
        orderId: data.orderId,
        customerId: data.customerId,
        paymentMethod: data.paymentMethod,
        token: data.token,
      },
    });
    return response.data;
  },

  updateStatus: async (id: number, status: string): Promise<Payment> => {
    const response = await apiClient.patch<Payment>(PAYMENT_ENDPOINTS.UPDATE_STATUS(id), null, {
      params: { status },
    });
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(PAYMENT_ENDPOINTS.DELETE(id));
  },

  getPaymentMethods: async (customerId: number): Promise<PaymentMethod[]> => {
    const response = await apiClient.get<PaymentMethod[]>(PAYMENT_ENDPOINTS.METHODS_BY_CUSTOMER(customerId));
    return response.data;
  },

  getPaymentMethod: async (id: number): Promise<PaymentMethod> => {
    const response = await apiClient.get<PaymentMethod>(PAYMENT_ENDPOINTS.METHOD_GET(id));
    return response.data;
  },

  addPaymentMethod: async (data: CreatePaymentMethodRequest): Promise<PaymentMethod> => {
    const response = await apiClient.post<PaymentMethod>(PAYMENT_ENDPOINTS.METHOD_CREATE, data);
    return response.data;
  },

  setDefaultPaymentMethod: async (customerId: number, methodId: number): Promise<PaymentMethod> => {
    const response = await apiClient.put<PaymentMethod>(
      PAYMENT_ENDPOINTS.METHOD_SET_DEFAULT(methodId),
      null,
      { params: { customerId } }
    );
    return response.data;
  },

  deletePaymentMethod: async (id: number): Promise<void> => {
    await apiClient.delete(PAYMENT_ENDPOINTS.METHOD_DELETE(id));
  },
};

export const usePayments = () => {
  return useQuery({
    queryKey: ['payments'],
    queryFn: paymentService.getAll,
    staleTime: 1000 * 60 * 2,
  });
};

export const usePayment = (id: number) => {
  return useQuery({
    queryKey: ['payment', id],
    queryFn: () => paymentService.getById(id),
    enabled: !!id,
  });
};

export const usePaymentsByOrder = (orderId: number) => {
  return useQuery({
    queryKey: ['payments', 'order', orderId],
    queryFn: () => paymentService.getByOrderId(orderId),
    enabled: !!orderId,
  });
};

export const usePaymentsByCustomer = (customerId: number) => {
  return useQuery({
    queryKey: ['payments', 'customer', customerId],
    queryFn: () => paymentService.getByCustomerId(customerId),
    enabled: !!customerId,
  });
};

export const useProcessPayment = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: ProcessPaymentRequest) => paymentService.process(data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['payments', 'order', variables.orderId] });
      queryClient.invalidateQueries({ queryKey: ['payments', 'customer', variables.customerId] });
    },
  });
};

export const usePaymentMethods = (customerId: number) => {
  return useQuery({
    queryKey: ['paymentMethods', 'customer', customerId],
    queryFn: () => paymentService.getPaymentMethods(customerId),
    enabled: !!customerId,
  });
};

export const useAddPaymentMethod = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreatePaymentMethodRequest) => paymentService.addPaymentMethod(data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['paymentMethods', 'customer', variables.customerId] });
    },
  });
};

export const useSetDefaultPaymentMethod = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ customerId, methodId }: { customerId: number; methodId: number }) =>
      paymentService.setDefaultPaymentMethod(customerId, methodId),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['paymentMethods', 'customer', variables.customerId] });
    },
  });
};

export const useDeletePayment = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => paymentService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['payments'] });
    },
  });
};
