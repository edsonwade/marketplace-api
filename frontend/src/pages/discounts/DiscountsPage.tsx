import { useState } from 'react';
import {
  Percent, DollarSign, Edit2, Trash2, Plus, Ticket,
  Calendar, X,
} from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';
import { Alert } from '../../components/ui/Alert';
import { Spinner } from '../../components/ui/Spinner';
import { Table, TableHead, TableBody, TableRow, TableHeadCell, TableCell } from '../../components/ui/Table';
import {
  useDiscounts, useCreateDiscount, useUpdateDiscount, useDeleteDiscount,
  useCoupons, useCreateCoupon, useDeleteCoupon,
} from '../../services';
import type { Discount, CreateDiscountRequest } from '../../api/types';

/* ================================================================
   TABS definition
   ================================================================ */
const TABS = [
  { key: 'discounts', label: 'Discounts', Icon: Percent },
  { key: 'coupons',   label: 'Coupons',   Icon: Ticket  },
] as const;
type TabKey = typeof TABS[number]['key'];

/* ================================================================
   DISCOUNTS TAB
   ================================================================ */
const EMPTY_FORM: CreateDiscountRequest = {
  name: '', description: '', discountType: 'PERCENTAGE',
  discountValue: 0, minPurchaseAmount: 0, maxDiscountAmount: 0,
  startDate: '', endDate: '',
};

const DiscountsTab = () => {
  const [modal, setModal] = useState<{ open: boolean; editing: Discount | null }>({ open: false, editing: null });
  const [form, setForm]   = useState<CreateDiscountRequest>(EMPTY_FORM);
  const [formError, setFormError] = useState<string | null>(null);
  const [deleteId, setDeleteId]   = useState<number | null>(null);

  const { data: discounts, isLoading, error } = useDiscounts();
  const createDiscount = useCreateDiscount();
  const updateDiscount = useUpdateDiscount();
  const deleteDiscount = useDeleteDiscount();

  const openCreate = () => {
    setForm(EMPTY_FORM);
    setFormError(null);
    setModal({ open: true, editing: null });
  };

  const openEdit = (d: Discount) => {
    setForm({
      name: d.name, description: d.description,
      discountType:      d.discountType as 'PERCENTAGE' | 'FIXED',
      discountValue:     Number(d.discountValue),
      minPurchaseAmount: Number(d.minPurchaseAmount || 0),
      maxDiscountAmount: Number(d.maxDiscountAmount || 0),
      startDate: d.startDate, endDate: d.endDate,
    });
    setFormError(null);
    setModal({ open: true, editing: d });
  };

  const validate = (): boolean => {
    if (!form.name.trim())        { setFormError('Name is required');                       return false; }
    if (!form.description.trim()) { setFormError('Description is required');                return false; }
    if (form.discountValue <= 0)  { setFormError('Discount value must be greater than 0'); return false; }
    if (!form.startDate)          { setFormError('Start date is required');                 return false; }
    if (!form.endDate)            { setFormError('End date is required');                   return false; }
    if (new Date(form.endDate) <= new Date(form.startDate)) {
      setFormError('End date must be after start date');
      return false;
    }
    return true;
  };

  const handleSubmit = async () => {
    if (!validate()) return;
    try {
      if (modal.editing) {
        await updateDiscount.mutateAsync({ id: modal.editing.id, data: form });
      } else {
        await createDiscount.mutateAsync(form);
      }
      setModal({ open: false, editing: null });
    } catch {
      setFormError('Failed to save discount');
    }
  };

  const handleDelete = async (id: number) => {
    await deleteDiscount.mutateAsync(id);
    setDeleteId(null);
  };

  if (isLoading) return <div className="py-16 flex justify-center"><Spinner size="lg" /></div>;
  if (error)     return <Alert variant="error" message="Failed to load discounts" className="max-w-lg" />;

  return (
    <>
      {/* Modal */}
      {modal.open && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4 overflow-y-auto" role="dialog" aria-modal="true">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg my-4">
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100">
              <h2 className="text-base font-semibold text-slate-800">
                {modal.editing ? 'Edit Discount' : 'New Discount'}
              </h2>
              <button type="button" aria-label="Close" onClick={() => setModal({ open: false, editing: null })} className="text-slate-400 hover:text-slate-600">
                <X className="h-5 w-5" aria-hidden="true" />
              </button>
            </div>
            <div className="px-6 py-5 space-y-4">
              {formError && <Alert variant="error" message={formError} />}
              <Input label="Name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="e.g., Summer Sale" />
              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-semibold text-slate-600 uppercase tracking-wide">Description</label>
                <textarea
                  value={form.description}
                  onChange={(e) => setForm({ ...form, description: e.target.value })}
                  rows={2}
                  placeholder="Describe this discount…"
                  className="px-3 py-2 text-sm border border-slate-300 rounded-lg bg-white hover:border-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-semibold text-slate-600 uppercase tracking-wide">Type</label>
                  <select
                    value={form.discountType}
                    onChange={(e) => setForm({ ...form, discountType: e.target.value as 'PERCENTAGE' | 'FIXED' })}
                    className="px-3 py-2 text-sm border border-slate-300 rounded-lg bg-white hover:border-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="PERCENTAGE">Percentage (%)</option>
                    <option value="FIXED">Fixed Amount ($)</option>
                  </select>
                </div>
                <Input
                  label={form.discountType === 'PERCENTAGE' ? 'Value (%)' : 'Value ($)'}
                  type="number" min={0}
                  value={form.discountValue}
                  onChange={(e) => setForm({ ...form, discountValue: parseFloat(e.target.value) || 0 })}
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <Input label="Min. Purchase ($)" type="number" min={0} value={form.minPurchaseAmount || ''} onChange={(e) => setForm({ ...form, minPurchaseAmount: parseFloat(e.target.value) || 0 })} />
                <Input label="Max. Discount ($)" type="number" min={0} value={form.maxDiscountAmount || ''} onChange={(e) => setForm({ ...form, maxDiscountAmount: parseFloat(e.target.value) || 0 })} />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <Input label="Start Date" type="datetime-local" value={form.startDate?.slice(0, 16)} onChange={(e) => setForm({ ...form, startDate: e.target.value })} />
                <Input label="End Date"   type="datetime-local" value={form.endDate?.slice(0, 16)}   onChange={(e) => setForm({ ...form, endDate: e.target.value })} />
              </div>
            </div>
            <div className="flex gap-3 px-6 pb-5">
              <Button onClick={handleSubmit} isLoading={createDiscount.isPending || updateDiscount.isPending} className="flex-1">
                {modal.editing ? 'Update' : 'Create'}
              </Button>
              <Button variant="outline" onClick={() => setModal({ open: false, editing: null })} className="flex-1">
                Cancel
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* Action row */}
      <div className="flex justify-end">
        <Button onClick={openCreate}>
          <Plus className="h-4 w-4" aria-hidden="true" />
          Add Discount
        </Button>
      </div>

      {/* Table */}
      <Table>
        <TableHead>
          <TableRow>
            <TableHeadCell>Name</TableHeadCell>
            <TableHeadCell>Type & Value</TableHeadCell>
            <TableHeadCell>Min. Purchase</TableHeadCell>
            <TableHeadCell>Valid Period</TableHeadCell>
            <TableHeadCell>Status</TableHeadCell>
            <TableHeadCell className="text-right">Actions</TableHeadCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {!discounts || discounts.length === 0 ? (
            <TableRow>
              <TableCell colSpan={6} className="text-center py-12 text-slate-400">
                <Percent className="h-8 w-8 mx-auto mb-2 opacity-30" aria-hidden="true" />
                No discounts yet
              </TableCell>
            </TableRow>
          ) : (
            discounts.map((d) => {
              const expired  = new Date(d.endDate) < new Date();
              const active   = d.isActive && !expired;
              return (
                <TableRow key={d.id}>
                  <TableCell>
                    <p className="font-semibold text-slate-800">{d.name}</p>
                    <p className="text-xs text-slate-400 truncate max-w-[180px]">{d.description}</p>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-1.5">
                      {d.discountType === 'PERCENTAGE'
                        ? <Percent className="h-3.5 w-3.5 text-violet-500" aria-hidden="true" />
                        : <DollarSign className="h-3.5 w-3.5 text-emerald-500" aria-hidden="true" />}
                      <span className="font-semibold">
                        {d.discountType === 'PERCENTAGE'
                          ? `${d.discountValue}%`
                          : `$${Number(d.discountValue).toFixed(2)}`}
                      </span>
                    </div>
                  </TableCell>
                  <TableCell className="text-slate-600">
                    ${Number(d.minPurchaseAmount || 0).toFixed(2)}
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-1.5 text-xs text-slate-500">
                      <Calendar className="h-3.5 w-3.5 shrink-0" aria-hidden="true" />
                      {new Date(d.startDate).toLocaleDateString()} →{' '}
                      {new Date(d.endDate).toLocaleDateString()}
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge variant={active ? 'success' : 'danger'} dot>
                      {active ? 'Active' : expired ? 'Expired' : 'Inactive'}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center justify-end gap-1.5">
                      {deleteId === d.id ? (
                        <>
                          <span className="text-xs text-slate-500 mr-1">Delete?</span>
                          <Button size="sm" variant="danger" onClick={() => handleDelete(d.id)} isLoading={deleteDiscount.isPending}>Confirm</Button>
                          <Button size="sm" variant="outline" onClick={() => setDeleteId(null)}>Cancel</Button>
                        </>
                      ) : (
                        <>
                          <Button size="sm" variant="outline" aria-label={`Edit ${d.name}`} onClick={() => openEdit(d)}>
                            <Edit2 className="h-3.5 w-3.5" aria-hidden="true" />
                          </Button>
                          <Button size="sm" variant="ghost" aria-label={`Delete ${d.name}`} onClick={() => setDeleteId(d.id)} className="text-red-500 hover:bg-red-50">
                            <Trash2 className="h-3.5 w-3.5" aria-hidden="true" />
                          </Button>
                        </>
                      )}
                    </div>
                  </TableCell>
                </TableRow>
              );
            })
          )}
        </TableBody>
      </Table>
    </>
  );
};

/* ================================================================
   COUPONS TAB
   ================================================================ */
const CouponsTab = () => {
  const [modal, setModal] = useState(false);
  const [couponForm, setCouponForm] = useState({ code: '', discountId: '', usageLimit: '' });
  const [formError, setFormError]   = useState<string | null>(null);
  const [deleteId, setDeleteId]     = useState<number | null>(null);

  const { data: coupons,   isLoading, error } = useCoupons();
  const { data: discounts } = useDiscounts();
  const createCoupon = useCreateCoupon();
  const deleteCoupon = useDeleteCoupon();

  const handleCreate = async () => {
    if (!couponForm.code.trim())    { setFormError('Coupon code is required');    return; }
    if (!couponForm.discountId)     { setFormError('Please select a discount');   return; }
    try {
      setFormError(null);
      await createCoupon.mutateAsync({
        data: {
          code:       couponForm.code.toUpperCase(),
          usageLimit: couponForm.usageLimit ? parseInt(couponForm.usageLimit) : undefined,
        },
        discountId: parseInt(couponForm.discountId),
      });
      setModal(false);
      setCouponForm({ code: '', discountId: '', usageLimit: '' });
    } catch {
      setFormError('Failed to create coupon');
    }
  };

  const handleDelete = async (id: number) => {
    await deleteCoupon.mutateAsync(id);
    setDeleteId(null);
  };

  if (isLoading) return <div className="py-16 flex justify-center"><Spinner size="lg" /></div>;
  if (error)     return <Alert variant="error" message="Failed to load coupons" className="max-w-lg" />;

  return (
    <>
      {/* Modal */}
      {modal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" role="dialog" aria-modal="true">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm">
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100">
              <h2 className="text-base font-semibold text-slate-800">Create Coupon</h2>
              <button type="button" aria-label="Close" onClick={() => setModal(false)} className="text-slate-400 hover:text-slate-600">
                <X className="h-5 w-5" aria-hidden="true" />
              </button>
            </div>
            <div className="px-6 py-5 space-y-4">
              {formError && <Alert variant="error" message={formError} />}
              <Input
                label="Coupon Code"
                value={couponForm.code}
                onChange={(e) => setCouponForm({ ...couponForm, code: e.target.value.toUpperCase() })}
                placeholder="e.g., SUMMER20"
              />
              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-semibold text-slate-600 uppercase tracking-wide">Linked Discount</label>
                <select
                  value={couponForm.discountId}
                  onChange={(e) => setCouponForm({ ...couponForm, discountId: e.target.value })}
                  className="px-3 py-2 text-sm border border-slate-300 rounded-lg bg-white hover:border-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">Select a discount…</option>
                  {discounts?.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.name} — {d.discountType === 'PERCENTAGE' ? `${d.discountValue}%` : `$${Number(d.discountValue).toFixed(2)}`}
                    </option>
                  ))}
                </select>
              </div>
              <Input
                label="Usage Limit (optional)"
                type="number" min={1}
                value={couponForm.usageLimit}
                onChange={(e) => setCouponForm({ ...couponForm, usageLimit: e.target.value })}
                placeholder="Leave empty for unlimited"
                hint="Leave blank for unlimited uses"
              />
            </div>
            <div className="flex gap-3 px-6 pb-5">
              <Button onClick={handleCreate} isLoading={createCoupon.isPending} className="flex-1">Create</Button>
              <Button variant="outline" onClick={() => setModal(false)} className="flex-1">Cancel</Button>
            </div>
          </div>
        </div>
      )}

      <div className="flex justify-end">
        <Button onClick={() => { setFormError(null); setModal(true); }}>
          <Plus className="h-4 w-4" aria-hidden="true" />
          Create Coupon
        </Button>
      </div>

      <Table>
        <TableHead>
          <TableRow>
            <TableHeadCell>Code</TableHeadCell>
            <TableHeadCell>Linked Discount</TableHeadCell>
            <TableHeadCell>Usage</TableHeadCell>
            <TableHeadCell>Status</TableHeadCell>
            <TableHeadCell className="text-right">Actions</TableHeadCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {!coupons || coupons.length === 0 ? (
            <TableRow>
              <TableCell colSpan={5} className="text-center py-12 text-slate-400">
                <Ticket className="h-8 w-8 mx-auto mb-2 opacity-30" aria-hidden="true" />
                No coupons yet
              </TableCell>
            </TableRow>
          ) : (
            coupons.map((c) => {
              const active = c.isActive && (!c.discount || c.discount.isActive);
              return (
                <TableRow key={c.id}>
                  <TableCell>
                    <code className="px-2 py-1 bg-slate-100 text-slate-800 rounded-md font-mono text-xs font-semibold tracking-wider">
                      {c.code}
                    </code>
                  </TableCell>
                  <TableCell>
                    {c.discount ? (
                      <div>
                        <p className="font-medium text-slate-800 text-sm">{c.discount.name}</p>
                        <p className="text-xs text-slate-500">
                          {c.discount.discountType === 'PERCENTAGE'
                            ? `${c.discount.discountValue}% off`
                            : `$${Number(c.discount.discountValue).toFixed(2)} off`}
                        </p>
                      </div>
                    ) : <span className="text-slate-400">—</span>}
                  </TableCell>
                  <TableCell>
                    {c.usageLimit != null
                      ? (
                        <div>
                          <span className="font-semibold text-slate-800">{c.usageCount || 0}</span>
                          <span className="text-slate-400 text-xs"> / {c.usageLimit}</span>
                        </div>
                      )
                      : <span className="text-slate-500 text-xs">Unlimited</span>}
                  </TableCell>
                  <TableCell>
                    <Badge variant={active ? 'success' : 'danger'} dot>
                      {active ? 'Active' : 'Inactive'}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <div className="flex justify-end gap-1.5">
                      {deleteId === c.id ? (
                        <>
                          <span className="text-xs text-slate-500 mr-1">Delete?</span>
                          <Button size="sm" variant="danger" onClick={() => handleDelete(c.id)} isLoading={deleteCoupon.isPending}>Confirm</Button>
                          <Button size="sm" variant="outline" onClick={() => setDeleteId(null)}>Cancel</Button>
                        </>
                      ) : (
                        <Button size="sm" variant="ghost" aria-label={`Delete coupon ${c.code}`} onClick={() => setDeleteId(c.id)} className="text-red-500 hover:bg-red-50">
                          <Trash2 className="h-3.5 w-3.5" aria-hidden="true" />
                        </Button>
                      )}
                    </div>
                  </TableCell>
                </TableRow>
              );
            })
          )}
        </TableBody>
      </Table>
    </>
  );
};

/* ================================================================
   PAGE
   ================================================================ */
export const DiscountsPage = () => {
  const [tab, setTab] = useState<TabKey>('discounts');

  return (
    <div className="space-y-6 max-w-7xl">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Discounts & Coupons</h1>
        <p className="text-sm text-slate-500 mt-0.5">Manage promotions, discount rules and coupon codes</p>
      </div>

      {/* Tabs */}
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

      <div role="tabpanel">
        {tab === 'discounts' ? <DiscountsTab /> : <CouponsTab />}
      </div>
    </div>
  );
};
