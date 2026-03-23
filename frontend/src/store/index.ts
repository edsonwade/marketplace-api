import { create } from 'zustand';
import type { Customer } from '../api/types';
import { cookies } from '../utils/cookies';

interface AuthState {
  isAuthenticated: boolean;
  user: Customer | null;
  role: string | null;  // 'USER' | 'ADMIN' | 'MANAGER'
  setAuth: (user?: Customer, role?: string) => void;
  setUser: (user: Customer) => void;
  setRole: (role: string) => void;
  clearAuth: () => void;
  isAdmin: () => boolean;
}

export const useAuthStore = create<AuthState>()((set, get) => ({
  isAuthenticated: !!cookies.get('refresh_token'),
  user: null,
  role: null,

  setAuth: (user, role) =>
    set({
      isAuthenticated: true,
      user: user || null,
      role: role || null,
    }),

  setUser: (user) =>
    set({ user }),

  setRole: (role) =>
    set({ role }),

  clearAuth: () =>
    set({
      isAuthenticated: false,
      user: null,
      role: null,
    }),

  isAdmin: () => {
    const r = get().role;
    return r === 'ADMIN' || r === 'MANAGER';
  },
}));

interface CartState {
  items: Array<{
    productId: number;
    productName: string;
    quantity: number;
    price: number;
  }>;
  addItem: (item: CartState['items'][0]) => void;
  removeItem: (productId: number) => void;
  updateQuantity: (productId: number, quantity: number) => void;
  clearCart: () => void;
  getTotal: () => number;
}

export const useCartStore = create<CartState>((set, get) => ({
  items: [],

  addItem: (item) =>
    set((state) => {
      const existing = state.items.find((i) => i.productId === item.productId);
      if (existing) {
        return {
          items: state.items.map((i) =>
            i.productId === item.productId
              ? { ...i, quantity: i.quantity + item.quantity }
              : i
          ),
        };
      }
      return { items: [...state.items, item] };
    }),

  removeItem: (productId) =>
    set((state) => ({
      items: state.items.filter((i) => i.productId !== productId),
    })),

  updateQuantity: (productId, quantity) =>
    set((state) => ({
      items: state.items.map((i) =>
        i.productId === productId ? { ...i, quantity } : i
      ),
    })),

  clearCart: () => set({ items: [] }),

  getTotal: () => {
    const items = get().items;
    return items.reduce((total, item) => total + item.price * item.quantity, 0);
  },
}));
