import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import apiClient from '../../api/client/apiClient';
import type { Discount, Coupon, CreateDiscountRequest, CreateCouponRequest, ApplyCouponRequest, ApplyCouponResponse } from '../../api/types';

const DISCOUNT_ENDPOINTS = {
  LIST: '/api/v1/discounts',
  GET: (id: number) => `/api/v1/discounts/${id}`,
  ACTIVE: '/api/v1/discounts/active',
  CREATE: '/api/v1/discounts',
  UPDATE: (id: number) => `/api/v1/discounts/${id}`,
  DELETE: (id: number) => `/api/v1/discounts/${id}`,
  COUPONS_LIST: '/api/v1/discounts/coupons',
  COUPON_GET: (id: number) => `/api/v1/discounts/coupons/${id}`,
  COUPON_BY_CODE: (code: string) => `/api/v1/discounts/coupons/code/${code}`,
  COUPON_ACTIVE: '/api/v1/discounts/coupons/active',
  COUPON_CREATE: '/api/v1/discounts/coupons',
  COUPON_UPDATE: (id: number) => `/api/v1/discounts/coupons/${id}`,
  COUPON_DELETE: (id: number) => `/api/v1/discounts/coupons/${id}`,
  COUPON_VALIDATE: '/api/v1/discounts/coupons/validate',
  COUPON_APPLY: '/api/v1/discounts/coupons/apply',
};

export const discountService = {
  getAll: async (): Promise<Discount[]> => {
    const response = await apiClient.get<Discount[]>(DISCOUNT_ENDPOINTS.LIST);
    return response.data;
  },

  getActive: async (): Promise<Discount[]> => {
    const response = await apiClient.get<Discount[]>(DISCOUNT_ENDPOINTS.ACTIVE);
    return response.data;
  },

  getById: async (id: number): Promise<Discount> => {
    const response = await apiClient.get<Discount>(DISCOUNT_ENDPOINTS.GET(id));
    return response.data;
  },

  create: async (data: CreateDiscountRequest): Promise<Discount> => {
    const response = await apiClient.post<Discount>(DISCOUNT_ENDPOINTS.CREATE, data);
    return response.data;
  },

  update: async (id: number, data: Partial<Omit<Discount, 'id'>>): Promise<Discount> => {
    const response = await apiClient.put<Discount>(DISCOUNT_ENDPOINTS.UPDATE(id), { id, ...data });
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(DISCOUNT_ENDPOINTS.DELETE(id));
  },

  getAllCoupons: async (): Promise<Coupon[]> => {
    const response = await apiClient.get<Coupon[]>(DISCOUNT_ENDPOINTS.COUPONS_LIST);
    return response.data;
  },

  getActiveCoupons: async (): Promise<Coupon[]> => {
    const response = await apiClient.get<Coupon[]>(DISCOUNT_ENDPOINTS.COUPON_ACTIVE);
    return response.data;
  },

  getCouponById: async (id: number): Promise<Coupon> => {
    const response = await apiClient.get<Coupon>(DISCOUNT_ENDPOINTS.COUPON_GET(id));
    return response.data;
  },

  getCouponByCode: async (code: string): Promise<Coupon> => {
    const response = await apiClient.get<Coupon>(DISCOUNT_ENDPOINTS.COUPON_BY_CODE(code));
    return response.data;
  },

  createCoupon: async (data: CreateCouponRequest, discountId: number): Promise<Coupon> => {
    const response = await apiClient.post<Coupon>(DISCOUNT_ENDPOINTS.COUPON_CREATE, data, {
      params: { discountId },
    });
    return response.data;
  },

  updateCoupon: async (id: number, data: Partial<Coupon>): Promise<Coupon> => {
    const response = await apiClient.put<Coupon>(DISCOUNT_ENDPOINTS.COUPON_UPDATE(id), data);
    return response.data;
  },

  deleteCoupon: async (id: number): Promise<void> => {
    await apiClient.delete(DISCOUNT_ENDPOINTS.COUPON_DELETE(id));
  },

  validateCoupon: async (code: string): Promise<boolean> => {
    const response = await apiClient.post<{ valid: boolean }>(DISCOUNT_ENDPOINTS.COUPON_VALIDATE, { code });
    return response.data.valid;
  },

  applyCoupon: async (data: ApplyCouponRequest): Promise<ApplyCouponResponse> => {
    const response = await apiClient.post<ApplyCouponResponse>(DISCOUNT_ENDPOINTS.COUPON_APPLY, data);
    return response.data;
  },
};

export const useDiscounts = () => {
  return useQuery({
    queryKey: ['discounts'],
    queryFn: discountService.getAll,
    staleTime: 1000 * 60 * 5,
  });
};

export const useActiveDiscounts = () => {
  return useQuery({
    queryKey: ['discounts', 'active'],
    queryFn: discountService.getActive,
    staleTime: 1000 * 60 * 2,
  });
};

export const useDiscount = (id: number) => {
  return useQuery({
    queryKey: ['discount', id],
    queryFn: () => discountService.getById(id),
    enabled: !!id,
  });
};

export const useCreateDiscount = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateDiscountRequest) => discountService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['discounts'] });
    },
  });
};

export const useUpdateDiscount = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Omit<Discount, 'id'>> }) =>
      discountService.update(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['discounts'] });
      queryClient.invalidateQueries({ queryKey: ['discount', variables.id] });
    },
  });
};

export const useDeleteDiscount = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => discountService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['discounts'] });
    },
  });
};

export const useCoupons = () => {
  return useQuery({
    queryKey: ['coupons'],
    queryFn: discountService.getAllCoupons,
    staleTime: 1000 * 60 * 5,
  });
};

export const useActiveCoupons = () => {
  return useQuery({
    queryKey: ['coupons', 'active'],
    queryFn: discountService.getActiveCoupons,
    staleTime: 1000 * 60 * 2,
  });
};

export const useCoupon = (id: number) => {
  return useQuery({
    queryKey: ['coupon', id],
    queryFn: () => discountService.getCouponById(id),
    enabled: !!id,
  });
};

export const useCreateCoupon = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ data, discountId }: { data: CreateCouponRequest; discountId: number }) =>
      discountService.createCoupon(data, discountId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['coupons'] });
    },
  });
};

export const useValidateCoupon = () => {
  return useMutation({
    mutationFn: (code: string) => discountService.validateCoupon(code),
  });
};

export const useApplyCoupon = () => {
  return useMutation({
    mutationFn: (data: ApplyCouponRequest) => discountService.applyCoupon(data),
  });
};

export const useDeleteCoupon = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => discountService.deleteCoupon(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['coupons'] });
    },
  });
};
