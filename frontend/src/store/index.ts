import { create } from 'zustand';
import type { Customer } from '../api/types';
import { cookies } from '../utils/cookies';

interface AuthState {
  isAuthenticated: boolean;
  user: Customer | null;
  setAuth: (user?: Customer) => void;
  setUser: (user: Customer) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>()((set) => ({
  isAuthenticated: !!cookies.get('refresh_token'),
  user: null,

  setAuth: (user) =>
    set({
      isAuthenticated: true,
      user: user || null,
    }),

  setUser: (user) =>
    set({
      user,
    }),

  clearAuth: () =>
    set({
      isAuthenticated: false,
      user: null,
    }),
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
