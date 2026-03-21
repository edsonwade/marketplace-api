import { useState } from 'react';
import { CreditCard, CheckCircle2, XCircle, Clock, RefreshCw, Plus, X } from 'lucide-react';
import { Card, CardBody } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';
import { Alert } from '../../components/ui/Alert';
import { Spinner } from '../../components/ui/Spinner';
import { Table, TableHead, TableBody, TableRow, TableHeadCell, TableCell } from '../../components/ui/Table';
import {
  usePayments,
  usePaymentMethods,
  useProcessPayment,
  useSetDefaultPaymentMethod,
  useAddPaymentMethod,
} from '../../services';

/* ---- Status config ---- */
type StatusKey = 'COMPLETED' | 'FAILED' | 'PENDING' | 'PROCESSING';
const STATUS_CFG: Record<StatusKey, { variant: 'success' | 'danger' | 'warning' | 'default'; Icon: typeof CheckCircle2; label: string }> = {
  COMPLETED:  { variant: 'success', Icon: CheckCircle2, label: 'Completed'  },
  FAILED:     { variant: 'danger',  Icon: XCircle,      label: 'Failed'     },
  PENDING:    { variant: 'warning', Icon: Clock,        label: 'Pending'    },
  PROCESSING: { variant: 'default', Icon: RefreshCw,    label: 'Processing' },
};

/* ================================================================
   TAB: Payments list + process form
   ================================================================ */
const PaymentsTab = () => {
  const [form, setForm] = useState({ orderId: '', customerId: '', paymentMethod: 'CREDIT_CARD' });
  const [formError,   setFormError]   = useState<string | null>(null);
  const [formSuccess, setFormSuccess] = useState(false);

  const { data: payments, isLoading, error } = usePayments();
  const processPayment = useProcessPayment();

  const handleProcess = async () => {
    if (!form.orderId || !form.customerId) {
      setFormError('Order ID and Customer ID are required');
      return;
    }
    try {
      setFormError(null);
      setFormSuccess(false);
      await processPayment.mutateAsync({
        orderId:       parseInt(form.orderId),
        customerId:    parseInt(form.customerId),
        paymentMethod: form.paymentMethod,
      });
      setFormSuccess(true);
      setForm({ orderId: '', customerId: '', paymentMethod: 'CREDIT_CARD' });
    } catch {
      setFormError('Failed to process payment. Please check the order and customer IDs.');
    }
  };

  if (isLoading) return <div className="py-16 flex justify-center"><Spinner size="lg" /></div>;
  if (error)     return <Alert variant="error" message="Failed to load payments" className="max-w-lg" />;

  return (
    <div className="space-y-6">
      {/* Process payment card */}
      <Card>
        <div className="px-5 py-4 border-b border-slate-100">
          <h2 className="text-sm font-semibold text-slate-800">Process New Payment</h2>
        </div>
        <CardBody className="flex flex-wrap gap-4 items-end">
          <Input
            label="Order ID"
            type="number"
            min={1}
            value={form.orderId}
            onChange={(e) => setForm({ ...form, orderId: e.target.value })}
            className="w-36"
            placeholder="e.g., 42"
          />
          <Input
            label="Customer ID"
            type="number"
            min={1}
            value={form.customerId}
            onChange={(e) => setForm({ ...form, customerId: e.target.value })}
            className="w-36"
            placeholder="e.g., 7"
          />
          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-semibold text-slate-600 uppercase tracking-wide">
              Payment Method
            </label>
            <select
              value={form.paymentMethod}
              onChange={(e) => setForm({ ...form, paymentMethod: e.target.value })}
              className="px-3 py-2 text-sm border border-slate-300 rounded-lg bg-white hover:border-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="CREDIT_CARD">Credit Card</option>
              <option value="DEBIT_CARD">Debit Card</option>
              <option value="PAYPAL">PayPal</option>
              <option value="BANK_TRANSFER">Bank Transfer</option>
            </select>
          </div>
          <Button onClick={handleProcess} isLoading={processPayment.isPending}>
            Process Payment
          </Button>
        </CardBody>
        {(formError || formSuccess) && (
          <div className="px-5 pb-4">
            {formError   && <Alert variant="error"   message={formError} />}
            {formSuccess && <Alert variant="success" message="Payment processed successfully!" />}
          </div>
        )}
      </Card>

      {/* Payments table */}
      <Table>
        <TableHead>
          <TableRow>
            <TableHeadCell>ID</TableHeadCell>
            <TableHeadCell>Order</TableHeadCell>
            <TableHeadCell>Customer</TableHeadCell>
            <TableHeadCell>Method</TableHeadCell>
            <TableHeadCell>Amount</TableHeadCell>
            <TableHeadCell>Status</TableHeadCell>
            <TableHeadCell>Date</TableHeadCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {!payments || payments.length === 0 ? (
            <TableRow>
              <TableCell colSpan={7} className="text-center py-12 text-slate-400">
                <CreditCard className="h-8 w-8 mx-auto mb-2 opacity-30" aria-hidden="true" />
                No payments found
              </TableCell>
            </TableRow>
          ) : (
            payments.map((p) => {
              const cfg = STATUS_CFG[(p.paymentStatus as StatusKey)] ?? STATUS_CFG.PENDING;
              return (
                <TableRow key={p.id}>
                  <TableCell className="font-mono text-xs text-slate-400">#{p.id}</TableCell>
                  <TableCell className="font-medium">#{p.orderId}</TableCell>
                  <TableCell className="text-slate-600">#{p.customerId}</TableCell>
                  <TableCell>
                    <span className="text-slate-600">{p.paymentMethod.replace('_', ' ')}</span>
                  </TableCell>
                  <TableCell className="font-semibold text-slate-800">
                    {p.currency} {Number(p.amount).toFixed(2)}
                  </TableCell>
                  <TableCell>
                    <Badge variant={cfg.variant} dot>{cfg.label}</Badge>
                  </TableCell>
                  <TableCell className="text-slate-500">
                    {new Date(p.createdAt).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })}
                  </TableCell>
                </TableRow>
              );
            })
          )}
        </TableBody>
      </Table>
    </div>
  );
};

/* ================================================================
   TAB: Payment Methods
   ================================================================ */
const PaymentMethodsTab = () => {
  const [customerId, setCustomerId] = useState('');
  const [showAdd, setShowAdd] = useState(false);
  const [addData, setAddData] = useState({
    customerId: '', methodType: 'CREDIT_CARD', provider: 'Stripe',
    lastFourDigits: '', expiryMonth: '', expiryYear: '',
  });
  const [addError, setAddError] = useState<string | null>(null);

  const { data: methods, isLoading, error } = usePaymentMethods(customerId ? parseInt(customerId) : 0);
  const addMethod  = useAddPaymentMethod();
  const setDefault = useSetDefaultPaymentMethod();

  const handleAdd = async () => {
    if (!addData.customerId || !addData.lastFourDigits || !addData.expiryMonth || !addData.expiryYear) {
      setAddError('All fields are required');
      return;
    }
    try {
      setAddError(null);
      await addMethod.mutateAsync({
        customerId:     parseInt(addData.customerId),
        methodType:     addData.methodType,
        provider:       addData.provider,
        lastFourDigits: addData.lastFourDigits,
        expiryMonth:    addData.expiryMonth,
        expiryYear:     addData.expiryYear,
        isDefault:      false,
      });
      setShowAdd(false);
      setAddData({ customerId: '', methodType: 'CREDIT_CARD', provider: 'Stripe', lastFourDigits: '', expiryMonth: '', expiryYear: '' });
    } catch {
      setAddError('Failed to add payment method');
    }
  };

  return (
    <div className="space-y-6">
      {/* Search row */}
      <Card>
        <CardBody className="flex flex-wrap gap-4 items-end">
          <Input
            label="Search by Customer ID"
            type="number"
            min={1}
            value={customerId}
            onChange={(e) => setCustomerId(e.target.value)}
            placeholder="Enter customer ID"
            className="w-52"
          />
          {customerId && (
            <Button variant="outline" onClick={() => { setCustomerId(''); setShowAdd(false); }}>
              <X className="h-4 w-4" aria-hidden="true" /> Clear
            </Button>
          )}
          {customerId && !showAdd && (
            <Button onClick={() => setShowAdd(true)}>
              <Plus className="h-4 w-4" aria-hidden="true" /> Add Payment Method
            </Button>
          )}
        </CardBody>
      </Card>

      {/* Add method form */}
      {showAdd && (
        <Card>
          <div className="flex items-center justify-between px-5 py-4 border-b border-slate-100">
            <h2 className="text-sm font-semibold text-slate-800">Add Payment Method</h2>
            <button type="button" aria-label="Close" onClick={() => setShowAdd(false)} className="text-slate-400 hover:text-slate-600">
              <X className="h-4 w-4" aria-hidden="true" />
            </button>
          </div>
          <CardBody className="flex flex-wrap gap-4 items-end">
            <Input label="Customer ID"   type="number" value={addData.customerId}     onChange={(e) => setAddData({ ...addData, customerId: e.target.value })}     className="w-32" />
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-semibold text-slate-600 uppercase tracking-wide">Type</label>
              <select value={addData.methodType} onChange={(e) => setAddData({ ...addData, methodType: e.target.value })} className="px-3 py-2 text-sm border border-slate-300 rounded-lg bg-white hover:border-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500">
                <option value="CREDIT_CARD">Credit Card</option>
                <option value="DEBIT_CARD">Debit Card</option>
                <option value="PAYPAL">PayPal</option>
              </select>
            </div>
            <Input label="Provider"      value={addData.provider}        onChange={(e) => setAddData({ ...addData, provider: e.target.value })}        className="w-28" />
            <Input label="Last 4 digits" value={addData.lastFourDigits}  onChange={(e) => setAddData({ ...addData, lastFourDigits: e.target.value })}  maxLength={4} className="w-28" placeholder="1234" />
            <Input label="Exp. Month"    value={addData.expiryMonth}     onChange={(e) => setAddData({ ...addData, expiryMonth: e.target.value })}     maxLength={2} className="w-20" placeholder="MM" />
            <Input label="Exp. Year"     type="number" value={addData.expiryYear} onChange={(e) => setAddData({ ...addData, expiryYear: e.target.value })} className="w-24" placeholder="YYYY" />
            <Button onClick={handleAdd} isLoading={addMethod.isPending}>Save</Button>
            <Button variant="outline" onClick={() => setShowAdd(false)}>Cancel</Button>
          </CardBody>
          {addError && <div className="px-5 pb-4"><Alert variant="error" message={addError} /></div>}
        </Card>
      )}

      {/* Methods table */}
      {error ? (
        <Alert variant="error" message="Failed to load payment methods" className="max-w-lg" />
      ) : isLoading ? (
        <div className="py-12 flex justify-center"><Spinner size="lg" /></div>
      ) : (
        <Table>
          <TableHead>
            <TableRow>
              <TableHeadCell>Type</TableHeadCell>
              <TableHeadCell>Provider</TableHeadCell>
              <TableHeadCell>Card</TableHeadCell>
              <TableHeadCell>Expiry</TableHeadCell>
              <TableHeadCell>Status</TableHeadCell>
              <TableHeadCell>Default</TableHeadCell>
              <TableHeadCell className="text-right">Actions</TableHeadCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {!methods || methods.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="text-center py-12 text-slate-400">
                  <CreditCard className="h-8 w-8 mx-auto mb-2 opacity-30" aria-hidden="true" />
                  {customerId ? 'No payment methods for this customer' : 'Enter a customer ID to search'}
                </TableCell>
              </TableRow>
            ) : (
              methods.map((m) => (
                <TableRow key={m.id}>
                  <TableCell className="font-medium text-slate-700">{m.methodType.replace('_', ' ')}</TableCell>
                  <TableCell className="text-slate-600">{m.provider || '—'}</TableCell>
                  <TableCell>
                    <span className="font-mono text-sm text-slate-800 tracking-wider">•••• {m.lastFourDigits}</span>
                  </TableCell>
                  <TableCell className="text-slate-600">{m.expiryMonth}/{m.expiryYear}</TableCell>
                  <TableCell>
                    <Badge variant={m.isActive ? 'success' : 'danger'} dot>
                      {m.isActive ? 'Active' : 'Inactive'}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    {m.isDefault
                      ? <Badge variant="info">Default</Badge>
                      : <span className="text-slate-300 text-sm">—</span>}
                  </TableCell>
                  <TableCell>
                    <div className="flex justify-end">
                      {!m.isDefault && (
                        <Button
                          size="sm"
                          variant="outline"
                          isLoading={setDefault.isPending}
                          onClick={() => setDefault.mutate({ customerId: m.customerId, methodId: m.id })}
                        >
                          Set Default
                        </Button>
                      )}
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      )}
    </div>
  );
};

/* ================================================================
   PAGE
   ================================================================ */
const TABS = [
  { key: 'payments', label: 'Payments',        Icon: CreditCard },
  { key: 'methods',  label: 'Payment Methods',  Icon: CreditCard },
] as const;
type TabKey = typeof TABS[number]['key'];

export const PaymentsPage = () => {
  const [tab, setTab] = useState<TabKey>('payments');

  return (
    <div className="space-y-6 max-w-7xl">

      <div>
        <h1 className="text-2xl font-bold text-slate-900">Payments</h1>
        <p className="text-sm text-slate-500 mt-0.5">Manage payments and payment methods</p>
      </div>

      {/* Tab bar */}
      <div className="flex border-b border-slate-200 gap-1" role="tablist">
        {TABS.map(({ key, label, Icon }) => (
          <button
            key={key}
            role="tab"
            aria-selected={tab === key}
            onClick={() => setTab(key)}
            className={`
              flex items-center gap-2 px-4 py-2.5 text-sm font-medium border-b-2 transition-colors
              ${tab === key
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-slate-500 hover:text-slate-700 hover:border-slate-300'}
            `}
          >
            <Icon className="h-4 w-4" aria-hidden="true" />
            {label}
          </button>
        ))}
      </div>

      {/* Tab panels */}
      <div role="tabpanel">
        {tab === 'payments' ? <PaymentsTab /> : <PaymentMethodsTab />}
      </div>
    </div>
  );
};
