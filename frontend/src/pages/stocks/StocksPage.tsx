import { useState } from 'react';
import { MapPin, Package, Edit2, Trash2, Plus, Warehouse, X } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';
import { Alert } from '../../components/ui/Alert';
import { Spinner } from '../../components/ui/Spinner';
import { Table, TableHead, TableBody, TableRow, TableHeadCell, TableCell } from '../../components/ui/Table';
import { useStocks, useCreateStock, useUpdateStockQuantity, useDeleteStock } from '../../services';
import type { CreateStockRequest } from '../../api/types';

export const StocksPage = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId]     = useState<number | null>(null);
  const [editQuantity, setEditQuantity] = useState(0);
  const [formData, setFormData]       = useState<CreateStockRequest>({ productId: 0, quantity: 0, location: '' });
  const [formError, setFormError]     = useState<string | null>(null);
  const [deleteId, setDeleteId]       = useState<number | null>(null);

  const { data: stocks, isLoading, error } = useStocks();
  const createStock = useCreateStock();
  const updateStock = useUpdateStockQuantity();
  const deleteStock = useDeleteStock();

  const handleCreate = async () => {
    if (!formData.productId)            { setFormError('Product ID is required');      return; }
    if (formData.quantity < 0)          { setFormError('Quantity cannot be negative'); return; }
    if (!formData.location.trim())      { setFormError('Location is required');        return; }
    try {
      await createStock.mutateAsync(formData);
      setIsModalOpen(false);
      setFormData({ productId: 0, quantity: 0, location: '' });
    } catch {
      setFormError('Failed to create stock entry');
    }
  };

  const handleUpdate = async (productId: number) => {
    if (editQuantity < 0) return;
    await updateStock.mutateAsync({ productId, quantity: editQuantity });
    setEditingId(null);
  };

  const handleDelete = async (id: number) => {
    await deleteStock.mutateAsync(id);
    setDeleteId(null);
  };

  if (isLoading) return <div className="py-16 flex justify-center"><Spinner size="lg" /></div>;
  if (error) return <Alert variant="error" message="Failed to load stock data" className="max-w-lg" />;

  return (
    <div className="space-y-6 max-w-7xl">

      {/* Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" role="dialog" aria-modal="true" aria-label="Add stock entry">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm">
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100">
              <h2 className="text-base font-semibold text-slate-800">Add Stock Entry</h2>
              <button type="button" aria-label="Close" onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-slate-600">
                <X className="h-5 w-5" aria-hidden="true" />
              </button>
            </div>
            <div className="px-6 py-5 space-y-4">
              {formError && <Alert variant="error" message={formError} />}
              <Input label="Product ID" type="number" min={1} value={formData.productId || ''} onChange={(e) => setFormData({ ...formData, productId: parseInt(e.target.value) || 0 })} placeholder="Enter product ID" />
              <Input label="Quantity"   type="number" min={0} value={formData.quantity  || ''} onChange={(e) => setFormData({ ...formData, quantity:  parseInt(e.target.value) || 0 })} placeholder="Enter quantity" />
              <Input label="Location"  value={formData.location} onChange={(e) => setFormData({ ...formData, location: e.target.value })} placeholder="e.g., Warehouse A, Shelf 3" />
            </div>
            <div className="flex gap-3 px-6 pb-5">
              <Button onClick={handleCreate} isLoading={createStock.isPending} className="flex-1">Create</Button>
              <Button variant="outline" onClick={() => setIsModalOpen(false)} className="flex-1">Cancel</Button>
            </div>
          </div>
        </div>
      )}

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Stock Management</h1>
          <p className="text-sm text-slate-500 mt-0.5">{stocks?.length ?? 0} stock entries</p>
        </div>
        <Button onClick={() => { setFormError(null); setIsModalOpen(true); }} className="self-start sm:self-auto">
          <Plus className="h-4 w-4" aria-hidden="true" />
          Add Stock Entry
        </Button>
      </div>

      <Table>
        <TableHead>
          <TableRow>
            <TableHeadCell>Stock ID</TableHeadCell>
            <TableHeadCell>Product</TableHeadCell>
            <TableHeadCell>Location</TableHeadCell>
            <TableHeadCell>Quantity</TableHeadCell>
            <TableHeadCell>Status</TableHeadCell>
            <TableHeadCell className="text-right">Actions</TableHeadCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {!stocks || stocks.length === 0 ? (
            <TableRow>
              <TableCell colSpan={6} className="text-center py-12 text-slate-400">
                <Warehouse className="h-8 w-8 mx-auto mb-2 opacity-30" aria-hidden="true" />
                No stock entries
              </TableCell>
            </TableRow>
          ) : (
            stocks.map((stock) => {
              const isOut  = stock.quantity === 0;
              const isLow  = stock.quantity > 0 && stock.quantity < 10;
              return (
                <TableRow key={stock.stockId}>
                  <TableCell className="font-mono text-xs text-slate-400">#{stock.stockId}</TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2 text-slate-700">
                      <Package className="h-4 w-4 text-slate-400 shrink-0" aria-hidden="true" />
                      <span>#{stock.productId}</span>
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-1.5 text-slate-600">
                      <MapPin className="h-3.5 w-3.5 text-slate-400 shrink-0" aria-hidden="true" />
                      {stock.location}
                    </div>
                  </TableCell>
                  <TableCell>
                    {editingId === stock.stockId ? (
                      <Input
                        type="number" min={0}
                        value={editQuantity}
                        onChange={(e) => setEditQuantity(parseInt(e.target.value) || 0)}
                        className="w-24"
                        aria-label="Edit quantity"
                      />
                    ) : (
                      <span className="font-semibold text-slate-800">{stock.quantity}</span>
                    )}
                  </TableCell>
                  <TableCell>
                    {isOut ? (
                      <Badge variant="danger" dot>Out of Stock</Badge>
                    ) : isLow ? (
                      <Badge variant="warning" dot>Low Stock</Badge>
                    ) : (
                      <Badge variant="success" dot>In Stock</Badge>
                    )}
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center justify-end gap-1.5">
                      {editingId === stock.stockId ? (
                        <>
                          <Button size="sm" onClick={() => handleUpdate(stock.productId)} isLoading={updateStock.isPending}>Save</Button>
                          <Button size="sm" variant="outline" onClick={() => setEditingId(null)}>Cancel</Button>
                        </>
                      ) : deleteId === stock.stockId ? (
                        <>
                          <span className="text-xs text-slate-500 mr-1">Delete?</span>
                          <Button size="sm" variant="danger" onClick={() => handleDelete(stock.stockId)} isLoading={deleteStock.isPending}>Confirm</Button>
                          <Button size="sm" variant="outline" onClick={() => setDeleteId(null)}>Cancel</Button>
                        </>
                      ) : (
                        <>
                          <Button size="sm" variant="outline" aria-label="Edit quantity" onClick={() => { setEditingId(stock.stockId); setEditQuantity(stock.quantity); }}>
                            <Edit2 className="h-3.5 w-3.5" aria-hidden="true" />
                          </Button>
                          <Button size="sm" variant="ghost" aria-label="Delete entry" onClick={() => setDeleteId(stock.stockId)} className="text-red-500 hover:bg-red-50">
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
    </div>
  );
};
