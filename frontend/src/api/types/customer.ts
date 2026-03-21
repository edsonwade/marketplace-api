export interface Customer {
  customerId: number;
  name: string;
  email: string;
  address: string;
}

export interface CreateCustomerRequest {
  name: string;
  email: string;
  address: string;
}

export interface UpdateCustomerRequest {
  name?: string;
  email?: string;
  address?: string;
}
