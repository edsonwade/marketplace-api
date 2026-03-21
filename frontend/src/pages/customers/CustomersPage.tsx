import { useState } from 'react';
import { Plus, Edit2, Trash2, X, Users, Search } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Alert } from '../../components/ui/Alert';
import { Spinner } from '../../components/ui/Spinner';
import { Table, TableHead, TableBody, TableRow, TableHeadCell, TableCell } from '../../components/ui/Table';
import { usePaginatedData } from '../../hooks/usePaginatedData';
import { Paginator } from '../../hooks/Paginator';
import { useCustomers, useCreateCustomer, useUpdateCustomer, useDeleteCustomer } from '../../services';
import type { Customer, CreateCustomerRequest } from '../../api/types';

interface ModalProps {
  customer: Customer | null;
  onClose: () => void;
  onSave: (data: CreateCustomerRequest, id?: number) => Promise<void>;
  isSaving: boolean;
}

const CustomerModal = ({ customer, onClose, onSave, isSaving }: ModalProps) => {
  const [form, setForm] = useState<CreateCustomerRequest>({
    name: customer?.name ?? '', email: customer?.email ?? '', address: customer?.address ?? '',
  });
  const [error, setError] = useState<string | null>(null);

  const handleSave = async () => {
    if (!form.name.trim())    { setError('Name is required');    return; }
    if (!form.email.trim())   { setError('Email is required');   return; }
    if (!form.address.trim()) { setError('Address is required'); return; }
    try { setError(null); await onSave(form, customer?.customerId); }
    catch { setError('Failed to save customer'); }
  };

  return (
    <div role="dialog" aria-modal="true" className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="bg-white dark:bg-slate-800 rounded-2xl shadow-2xl w-full max-w-md border border-slate-200 dark:border-slate-700">
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 dark:border-slate-700">
          <h2 className="text-base font-semibold text-slate-800 dark:text-slate-100">
            {customer ? 'Edit Customer' : 'Add Customer'}
          </h2>
          <button type="button" aria-label="Close" onClick={onClose} className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-200">
            <X className="h-5 w-5" aria-hidden="true" />
          </button>
        </div>
        <div className="px-6 py-5 space-y-4">
          {error && <Alert variant="error" message={error} />}
          <Input label="Name"    value={form.name}    onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Full name" />
          <Input label="Email"   value={form.email}   onChange={(e) => setForm({ ...form, email: e.target.value })} type="email" placeholder="email@company.com" />
          <Input label="Address" value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} placeholder="Street, city, country" />
        </div>
        <div className="flex gap-3 px-6 pb-5">
          <Button onClick={handleSave} isLoading={isSaving} className="flex-1">{customer ? 'Update' : 'Create'}</Button>
          <Button variant="outline" onClick={onClose} className="flex-1">Cancel</Button>
        </div>
      </div>
    </div>
  );
};

export const CustomersPage = () => {
  const [modal,    setModal]    = useState<{ open: boolean; customer: Customer | null }>({ open: false, customer: null });
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const { data: customers, isLoading, error } = useCustomers();
  const createCustomer = useCreateCustomer();
  const updateCustomer = useUpdateCustomer();
  const deleteCustomer = useDeleteCustomer();

  const { items, query, setQuery, page, setPage, totalPages, totalItems, pageSize } =
    usePaginatedData<Customer>({
      data: customers,
      pageSize: 10,
      filterFn: (c, q) =>
        c.name.toLowerCase().includes(q) ||
        c.email.toLowerCase().includes(q) ||
        c.address.toLowerCase().includes(q),
    });

  const handleSave = async (data: CreateCustomerRequest, id?: number) => {
    if (id !== undefined) await updateCustomer.mutateAsync({ id, data });
    else                  await createCustomer.mutateAsync(data);
    setModal({ open: false, customer: null });
  };

  const handleDelete = async (id: number) => {
    await deleteCustomer.mutateAsync(id);
    setDeleteId(null);
  };

  if (isLoading) return <div className="py-16 flex justify-center"><Spinner size="lg" /></div>;
  if (error)     return <Alert variant="error" message="Failed to load customers" className="max-w-lg" />;

  return (
    <div className="space-y-5 max-w-7xl">
      {modal.open && (
        <CustomerModal
          customer={modal.customer}
          onClose={() => setModal({ open: false, customer: null })}
          onSave={handleSave}
          isSaving={createCustomer.isPending || updateCustomer.isPending}
        />
      )}

      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white">Customers</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-0.5">{customers?.length ?? 0} registered</p>
        </div>
        <Button onClick={() => setModal({ open: true, customer: null })} className="self-start sm:self-auto">
          <Plus className="h-4 w-4" aria-hidden="true" /> Add Customer
        </Button>
      </div>

      {/* Search */}
      <div className="relative max-w-xs">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400 pointer-events-none" aria-hidden="true" />
        <input type="search" placeholder="Search name, email, address…" value={query} onChange={(e) => setQuery(e.target.value)}
          className="w-full pl-9 pr-3 py-2 text-sm border border-slate-300 dark:border-slate-600 rounded-lg bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
          aria-label="Search customers" />
      </div>

      <div>
        <Table>
          <TableHead>
            <TableRow>
              <TableHeadCell>ID</TableHeadCell>
              <TableHeadCell>Name</TableHeadCell>
              <TableHeadCell>Email</TableHeadCell>
              <TableHeadCell>Address</TableHeadCell>
              <TableHeadCell className="text-right">Actions</TableHeadCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {items.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="text-center py-10 text-slate-400 dark:text-slate-500">
                  <Users className="h-8 w-8 mx-auto mb-2 opacity-30" aria-hidden="true" />
                  {query ? 'No customers match your search' : 'No customers yet'}
                </TableCell>
              </TableRow>
            ) : items.map((c) => (
              <TableRow key={c.customerId}>
                <TableCell className="font-mono text-xs text-slate-400 dark:text-slate-500">#{c.customerId}</TableCell>
                <TableCell>
                  <div className="flex items-center gap-2.5">
                    <div aria-hidden="true" className="w-7 h-7 rounded-full bg-blue-100 dark:bg-blue-900/50 flex items-center justify-center text-blue-600 dark:text-blue-300 text-xs font-bold shrink-0">
                      {c.name?.charAt(0).toUpperCase()}
                    </div>
                    <span className="font-medium text-slate-800 dark:text-slate-100">{c.name}</span>
                  </div>
                </TableCell>
                <TableCell className="text-slate-600 dark:text-slate-300">{c.email}</TableCell>
                <TableCell className="text-slate-500 dark:text-slate-400 max-w-[180px] truncate">{c.address}</TableCell>
                <TableCell>
                  <div className="flex items-center justify-end gap-1.5">
                    {deleteId === c.customerId ? (
                      <>
                        <span className="text-xs text-slate-500 dark:text-slate-400 mr-1">Delete?</span>
                        <Button size="sm" variant="danger" onClick={() => handleDelete(c.customerId)} isLoading={deleteCustomer.isPending}>Confirm</Button>
                        <Button size="sm" variant="outline" onClick={() => setDeleteId(null)}>Cancel</Button>
                      </>
                    ) : (
                      <>
                        <Button size="sm" variant="outline" aria-label={`Edit ${c.name}`} onClick={() => setModal({ open: true, customer: c })}>
                          <Edit2 className="h-3.5 w-3.5" aria-hidden="true" />
                        </Button>
                        <Button size="sm" variant="ghost" aria-label={`Delete ${c.name}`} onClick={() => setDeleteId(c.customerId)} className="text-red-500 hover:bg-red-50 dark:hover:bg-red-900/30">
                          <Trash2 className="h-3.5 w-3.5" aria-hidden="true" />
                        </Button>
                      </>
                    )}
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <Paginator page={page} totalPages={totalPages} totalItems={totalItems} pageSize={pageSize} onPage={setPage} />
      </div>
    </div>
  );
};
