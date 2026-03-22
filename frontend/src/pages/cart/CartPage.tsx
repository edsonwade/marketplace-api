import { useState } from 'react';
import { Trash2, Plus, Minus, ShoppingCart, Package, ArrowRight, CreditCard, CheckCircle2, X } from 'lucide-react';
import { Card, CardBody, CardHeader } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Alert } from '../../components/ui/Alert';
import { Spinner } from '../../components/ui/Spinner';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store';
import {
  useBackendCart,
  useUpdateCartItem,
  useRemoveCartItem,
  useClearCart,
  useCheckoutAndCreateOrder,
} from '../../services/cart/service';
import { useProcessPayment } from '../../services/payment/service';

const PAYMENT_METHODS = [
  { value: 'CREDIT_CARD',   label: 'Credit Card'   },
  { value: 'DEBIT_CARD',    label: 'Debit Card'    },
  { value: 'PAYPAL',        label: 'PayPal'         },
  { value: 'BANK_TRANSFER', label: 'Bank Transfer' },
];

/* ── Payment modal ───────────────────────────────────────────────────────────── */
interface PaymentModalProps {
  orderId: number;
  customerId: number;
  total: number;
  onClose: () => void;
  onSuccess: () => void;
}

const PaymentModal = ({ orderId, customerId, total, onClose, onSuccess }: PaymentModalProps) => {
  const [method, setMethod] = useState('CREDIT_CARD');
  const [result, setResult] = useState<'idle' | 'success' | 'failed'>('idle');
  const processPayment = useProcessPayment();

  const handlePay = async () => {
    try {
      setResult('idle');
      await processPayment.mutateAsync({ orderId, customerId, paymentMethod: method });
      setResult('success');
      setTimeout(onSuccess, 1800);
    } catch {
      setResult('failed');
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4" role="dialog" aria-modal="true">
      <div className="bg-white dark:bg-slate-800 rounded-2xl shadow-2xl w-full max-w-sm border border-slate-200 dark:border-slate-700">

        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 dark:border-slate-700">
          <div className="flex items-center gap-2">
            <CreditCard className="h-4 w-4 text-blue-500" aria-hidden="true" />
            <h2 className="text-base font-semibold text-slate-800 dark:text-slate-100">Complete Payment</h2>
          </div>
          {result === 'idle' && (
            <button type="button" aria-label="Close" onClick={onClose}
              className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition-colors">
              <X className="h-5 w-5" aria-hidden="true" />
            </button>
          )}
        </div>

        <div className="px-6 py-5 space-y-4">
          {/* Result states */}
          {result === 'success' && (
            <div className="flex flex-col items-center gap-3 py-4">
              <div className="w-16 h-16 rounded-full bg-emerald-100 dark:bg-emerald-900/30 flex items-center justify-center">
                <CheckCircle2 className="h-8 w-8 text-emerald-500" aria-hidden="true" />
              </div>
              <p className="text-lg font-bold text-emerald-600 dark:text-emerald-400">Payment Successful!</p>
              <p className="text-sm text-slate-500 dark:text-slate-400">Order #{orderId} has been paid.</p>
            </div>
          )}

          {result === 'failed' && (
            <Alert variant="error" message="Payment failed. Please try a different method." />
          )}

          {result === 'idle' && (
            <>
              {/* Order summary */}
              <div className="bg-slate-50 dark:bg-slate-900/50 rounded-xl p-4 space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-slate-500 dark:text-slate-400">Order</span>
                  <span className="font-medium text-slate-700 dark:text-slate-200">#{orderId}</span>
                </div>
                <div className="flex justify-between text-sm border-t border-slate-200 dark:border-slate-700 pt-2">
                  <span className="font-semibold text-slate-700 dark:text-slate-200">Total</span>
                  <span className="text-xl font-bold text-slate-900 dark:text-white">${total.toFixed(2)}</span>
                </div>
              </div>

              {/* Payment method */}
              <div>
                <label className="block text-xs font-semibold text-slate-600 dark:text-slate-300 uppercase tracking-wide mb-2">
                  Payment Method
                </label>
                <div className="grid grid-cols-2 gap-2">
                  {PAYMENT_METHODS.map((m) => (
                    <button
                      key={m.value}
                      type="button"
                      onClick={() => setMethod(m.value)}
                      className={`px-3 py-2 text-xs font-medium rounded-lg border transition-all ${
                        method === m.value
                          ? 'bg-blue-600 text-white border-blue-600'
                          : 'bg-white dark:bg-slate-800 text-slate-600 dark:text-slate-300 border-slate-300 dark:border-slate-600 hover:border-blue-400'
                      }`}
                    >
                      {m.label}
                    </button>
                  ))}
                </div>
              </div>

              <Button onClick={handlePay} isLoading={processPayment.isPending} className="w-full" size="lg">
                {processPayment.isPending
                  ? 'Processing…'
                  : `Pay $${total.toFixed(2)}`}
              </Button>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

/* ── Empty state ─────────────────────────────────────────────────────────────── */
const EmptyCart = () => {
  const navigate = useNavigate();
  return (
    <div className="flex flex-col items-center justify-center py-24 gap-4 text-slate-400">
      <div className="w-16 h-16 rounded-2xl bg-slate-100 dark:bg-slate-800 flex items-center justify-center">
        <ShoppingCart className="h-8 w-8 opacity-50" aria-hidden="true" />
      </div>
      <div className="text-center">
        <p className="text-lg font-semibold text-slate-600 dark:text-slate-300">Your cart is empty</p>
        <p className="text-sm mt-1">Browse products and add them to your cart</p>
      </div>
      <Button onClick={() => navigate('/products')}>
        <Package className="h-4 w-4" aria-hidden="true" /> Browse Products
      </Button>
    </div>
  );
};

/* ── Main page ───────────────────────────────────────────────────────────────── */
export const CartPage = () => {
  const { user } = useAuthStore();
  const customerId = user?.customerId ? Number(user.customerId) : undefined;
  const navigate   = useNavigate();

  const { data: cart, isLoading, error } = useBackendCart(customerId);
  const updateItem    = useUpdateCartItem();
  const removeItem    = useRemoveCartItem();
  const clearCart     = useClearCart();
  const checkoutOrder = useCheckoutAndCreateOrder();

  const [paymentModal, setPaymentModal] = useState<{ orderId: number; total: number } | null>(null);
  const [checkoutError, setCheckoutError] = useState<string | null>(null);

  const handleCheckout = async () => {
    if (!customerId) return;
    setCheckoutError(null);
    try {
      const order = await checkoutOrder.mutateAsync(customerId);
      const total = Number(order.totalAmount ?? cart?.totalAmount ?? 0);
      setPaymentModal({ orderId: order.orderId, total });
    } catch (e: any) {
      setCheckoutError(e?.response?.data || 'Checkout failed. Please try again.');
    }
  };

  const handlePaymentSuccess = () => {
    setPaymentModal(null);
    navigate('/orders');
  };

  /* No customer ID (not logged in) */
  if (!customerId) return (
    <div className="flex flex-col items-center py-16 gap-4">
      <Alert variant="warning" message="Please log in to view your cart." />
      <Button onClick={() => navigate('/login')}>Sign In</Button>
    </div>
  );

  if (isLoading) return <div className="py-16 flex justify-center"><Spinner size="lg" /></div>;

  /* 404 = no active cart yet */
  const items = cart?.items ?? [];
  if (error || items.length === 0) return <EmptyCart />;

  const total = Number(cart?.totalAmount ?? 0);

  return (
    <div className="space-y-6 max-w-5xl">
      {/* Payment modal */}
      {paymentModal && customerId && (
        <PaymentModal
          orderId={paymentModal.orderId}
          customerId={customerId}
          total={paymentModal.total}
          onClose={() => setPaymentModal(null)}
          onSuccess={handlePaymentSuccess}
        />
      )}

      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white">Shopping Cart</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-0.5">
            {items.length} item{items.length !== 1 ? 's' : ''}
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={() => customerId && clearCart.mutate(customerId)}
          isLoading={clearCart.isPending}
          className="text-red-500 border-red-200 hover:bg-red-50 dark:hover:bg-red-900/20">
          <Trash2 className="h-3.5 w-3.5" aria-hidden="true" /> Clear all
        </Button>
      </div>

      {checkoutError && <Alert variant="error" message={checkoutError} onClose={() => setCheckoutError(null)} />}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Items list */}
        <div className="lg:col-span-2 space-y-3">
          {items.map((item) => (
            <Card key={item.productId}>
              <CardBody className="flex items-center gap-4 py-4">
                <div className="w-10 h-10 rounded-lg bg-blue-50 dark:bg-blue-900/30 flex items-center justify-center shrink-0">
                  <Package className="h-5 w-5 text-blue-500 dark:text-blue-400" aria-hidden="true" />
                </div>

                <div className="flex-1 min-w-0">
                  <p className="font-semibold text-slate-800 dark:text-slate-100 truncate">{item.productName}</p>
                  <p className="text-sm text-slate-500 dark:text-slate-400">
                    ${Number(item.price ?? 0).toFixed(2)} each
                  </p>
                </div>

                {/* Qty stepper */}
                <div className="flex items-center gap-1 bg-slate-100 dark:bg-slate-700 rounded-lg p-1"
                  role="group" aria-label={`Quantity for ${item.productName}`}>
                  <button type="button" aria-label="Decrease quantity"
                    onClick={() => customerId && updateItem.mutate({
                      customerId, productId: item.productId,
                      quantity: Math.max(0, (item.quantity ?? 1) - 1),
                    })}
                    className="w-7 h-7 flex items-center justify-center rounded-md hover:bg-white dark:hover:bg-slate-600 text-slate-600 dark:text-slate-300 transition-colors">
                    <Minus className="h-3.5 w-3.5" aria-hidden="true" />
                  </button>
                  <span aria-live="polite"
                    className="w-8 text-center text-sm font-semibold text-slate-800 dark:text-slate-100 select-none">
                    {item.quantity}
                  </span>
                  <button type="button" aria-label="Increase quantity"
                    onClick={() => customerId && updateItem.mutate({
                      customerId, productId: item.productId, quantity: (item.quantity ?? 1) + 1,
                    })}
                    className="w-7 h-7 flex items-center justify-center rounded-md hover:bg-white dark:hover:bg-slate-600 text-slate-600 dark:text-slate-300 transition-colors">
                    <Plus className="h-3.5 w-3.5" aria-hidden="true" />
                  </button>
                </div>

                <p className="font-bold text-slate-900 dark:text-white w-16 text-right shrink-0">
                  ${Number(item.subtotal ?? (Number(item.price) * (item.quantity ?? 1))).toFixed(2)}
                </p>

                <button type="button" aria-label={`Remove ${item.productName}`}
                  onClick={() => customerId && removeItem.mutate({ customerId, productId: item.productId })}
                  className="text-slate-300 dark:text-slate-600 hover:text-red-500 dark:hover:text-red-400 transition-colors ml-1">
                  <Trash2 className="h-4 w-4" aria-hidden="true" />
                </button>
              </CardBody>
            </Card>
          ))}
        </div>

        {/* Order summary */}
        <div className="lg:col-span-1">
          <Card className="sticky top-24">
            <CardHeader>
              <h2 className="text-sm font-semibold text-slate-800 dark:text-slate-100">Order Summary</h2>
            </CardHeader>
            <CardBody className="space-y-3">
              <div className="flex justify-between text-sm">
                <span className="text-slate-500 dark:text-slate-400">
                  Subtotal ({items.reduce((s, i) => s + (i.quantity ?? 1), 0)} items)
                </span>
                <span className="font-medium text-slate-700 dark:text-slate-200">${total.toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-slate-500 dark:text-slate-400">Shipping</span>
                <span className="text-emerald-600 dark:text-emerald-400 font-medium">Free</span>
              </div>
              <div className="border-t border-slate-100 dark:border-slate-700 pt-3 flex justify-between">
                <span className="font-semibold text-slate-800 dark:text-slate-200">Total</span>
                <span className="text-xl font-bold text-slate-900 dark:text-white">${total.toFixed(2)}</span>
              </div>

              {/* Flow indicator */}
              <div className="bg-blue-50 dark:bg-blue-900/20 rounded-xl p-3 text-xs text-blue-700 dark:text-blue-300 space-y-1.5">
                <p className="font-semibold">How checkout works:</p>
                <p>1. Click Checkout → creates an Order</p>
                <p>2. Choose payment method → pay</p>
                <p>3. Redirected to Orders page ✓</p>
              </div>

              <Button className="w-full" size="lg" onClick={handleCheckout}
                isLoading={checkoutOrder.isPending} disabled={items.length === 0}>
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
