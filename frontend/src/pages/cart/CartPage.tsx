import { Trash2, Plus, Minus, ShoppingCart, ArrowRight, Package } from 'lucide-react';
import { Card, CardBody } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { useCartStore } from '../../store';
import { useNavigate } from 'react-router-dom';

export const CartPage = () => {
  const { items, removeItem, updateQuantity, clearCart, getTotal } = useCartStore();
  const navigate = useNavigate();

  if (items.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-24 gap-4 text-slate-400">
        <div className="w-16 h-16 rounded-2xl bg-slate-100 flex items-center justify-center">
          <ShoppingCart className="h-8 w-8 opacity-50" aria-hidden="true" />
        </div>
        <div className="text-center">
          <p className="text-lg font-semibold text-slate-600">Your cart is empty</p>
          <p className="text-sm mt-1">Add products to get started</p>
        </div>
        <Button onClick={() => navigate('/products')}>
          <Package className="h-4 w-4" aria-hidden="true" />
          Browse Products
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-5xl">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Shopping Cart</h1>
          <p className="text-sm text-slate-500 mt-0.5">{items.length} item{items.length !== 1 ? 's' : ''}</p>
        </div>
        <Button variant="outline" size="sm" onClick={clearCart} className="text-red-500 border-red-200 hover:bg-red-50">
          <Trash2 className="h-3.5 w-3.5" aria-hidden="true" />
          Clear all
        </Button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

        {/* Items */}
        <div className="lg:col-span-2 space-y-3">
          {items.map((item) => (
            <Card key={item.productId}>
              <CardBody className="flex items-center gap-4 py-4">
                {/* Product icon */}
                <div className="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center shrink-0">
                  <Package className="h-5 w-5 text-blue-500" aria-hidden="true" />
                </div>

                {/* Name & price */}
                <div className="flex-1 min-w-0">
                  <p className="font-semibold text-slate-800 truncate">{item.productName}</p>
                  <p className="text-sm text-slate-500">${item.price.toFixed(2)} each</p>
                </div>

                {/* Qty stepper */}
                <div
                  className="flex items-center gap-1 bg-slate-100 rounded-lg p-1"
                  role="group"
                  aria-label={`Quantity for ${item.productName}`}
                >
                  <button
                    type="button"
                    aria-label="Decrease quantity"
                    onClick={() => updateQuantity(item.productId, Math.max(1, item.quantity - 1))}
                    className="w-7 h-7 flex items-center justify-center rounded-md hover:bg-white text-slate-600 transition-colors"
                  >
                    <Minus className="h-3.5 w-3.5" aria-hidden="true" />
                  </button>
                  <span
                    aria-live="polite"
                    className="w-8 text-center text-sm font-semibold text-slate-800 select-none"
                  >
                    {item.quantity}
                  </span>
                  <button
                    type="button"
                    aria-label="Increase quantity"
                    onClick={() => updateQuantity(item.productId, item.quantity + 1)}
                    className="w-7 h-7 flex items-center justify-center rounded-md hover:bg-white text-slate-600 transition-colors"
                  >
                    <Plus className="h-3.5 w-3.5" aria-hidden="true" />
                  </button>
                </div>

                {/* Subtotal */}
                <p className="font-bold text-slate-900 w-16 text-right shrink-0">
                  ${(item.price * item.quantity).toFixed(2)}
                </p>

                {/* Remove */}
                <button
                  type="button"
                  aria-label={`Remove ${item.productName} from cart`}
                  onClick={() => removeItem(item.productId)}
                  className="text-slate-300 hover:text-red-500 transition-colors ml-1"
                >
                  <Trash2 className="h-4 w-4" aria-hidden="true" />
                </button>
              </CardBody>
            </Card>
          ))}
        </div>

        {/* Order Summary */}
        <div className="lg:col-span-1">
          <Card className="sticky top-24">
            <div className="px-5 py-4 border-b border-slate-100">
              <h2 className="text-sm font-semibold text-slate-800">Order Summary</h2>
            </div>
            <CardBody className="space-y-3">
              <div className="flex justify-between text-sm">
                <span className="text-slate-500">
                  Subtotal ({items.reduce((s, i) => s + i.quantity, 0)} items)
                </span>
                <span className="font-medium">${getTotal().toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-slate-500">Discount</span>
                <span className="text-emerald-600 font-medium">— $0.00</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-slate-500">Shipping</span>
                <span className="text-emerald-600 font-medium">Free</span>
              </div>
              <div className="border-t border-slate-100 pt-3 flex justify-between">
                <span className="font-semibold text-slate-800">Total</span>
                <span className="text-xl font-bold text-slate-900">${getTotal().toFixed(2)}</span>
              </div>
              <Button className="w-full mt-2" size="lg">
                Checkout
                <ArrowRight className="h-4 w-4" aria-hidden="true" />
              </Button>
            </CardBody>
          </Card>
        </div>
      </div>
    </div>
  );
};
