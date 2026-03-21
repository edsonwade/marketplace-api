export interface Discount {
  id: number;
  name: string;
  description: string;
  discountType: string;
  discountValue: number;
  minPurchaseAmount?: number;
  maxDiscountAmount?: number;
  startDate: string;
  endDate: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface Coupon {
  id: number;
  code: string;
  discountId: number;
  discount?: Discount;
  usageLimit?: number;
  usageCount?: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface CreateDiscountRequest {
  name: string;
  description: string;
  discountType: 'PERCENTAGE' | 'FIXED';
  discountValue: number;
  minPurchaseAmount?: number;
  maxDiscountAmount?: number;
  startDate: string;
  endDate: string;
}

export interface CreateCouponRequest {
  code: string;
  usageLimit?: number;
}

export interface ValidateCouponRequest {
  code: string;
}

export interface ApplyCouponRequest {
  code: string;
  amount: number;
  customerId: number;
}

export interface ApplyCouponResponse {
  discount: number;
  finalAmount: number;
}
