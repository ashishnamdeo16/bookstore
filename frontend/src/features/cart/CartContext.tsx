import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import type { CartItem } from '../../types/order';

const STORAGE_KEY = 'bookstore.cart';

interface CartContextValue {
  items: CartItem[];
  itemCount: number;
  subtotal: number;
  addItem: (item: Omit<CartItem, 'quantity'>, quantity?: number) => void;
  updateQuantity: (bookId: string, quantity: number) => void;
  removeItem: (bookId: string) => void;
  clear: () => void;
}

const CartContext = createContext<CartContextValue | null>(null);

function loadCart(): CartItem[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw) as CartItem[];
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

function saveCart(items: CartItem[]): void {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
}

export function CartProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<CartItem[]>(() => loadCart());

  useEffect(() => {
    saveCart(items);
  }, [items]);

  const addItem = useCallback((item: Omit<CartItem, 'quantity'>, quantity = 1) => {
    const qty = Math.max(1, quantity);
    setItems((current) => {
      const existing = current.find((entry) => entry.bookId === item.bookId);
      if (existing) {
        return current.map((entry) =>
          entry.bookId === item.bookId
            ? { ...entry, quantity: entry.quantity + qty, title: item.title, price: item.price }
            : entry,
        );
      }
      return [...current, { ...item, quantity: qty }];
    });
  }, []);

  const updateQuantity = useCallback((bookId: string, quantity: number) => {
    setItems((current) => {
      if (quantity < 1) {
        return current.filter((entry) => entry.bookId !== bookId);
      }
      return current.map((entry) =>
        entry.bookId === bookId ? { ...entry, quantity } : entry,
      );
    });
  }, []);

  const removeItem = useCallback((bookId: string) => {
    setItems((current) => current.filter((entry) => entry.bookId !== bookId));
  }, []);

  const clear = useCallback(() => setItems([]), []);

  const value = useMemo<CartContextValue>(() => {
    const itemCount = items.reduce((sum, item) => sum + item.quantity, 0);
    const subtotal = items.reduce((sum, item) => sum + item.price * item.quantity, 0);
    return { items, itemCount, subtotal, addItem, updateQuantity, removeItem, clear };
  }, [items, addItem, updateQuantity, removeItem, clear]);

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart(): CartContextValue {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within CartProvider');
  }
  return context;
}
