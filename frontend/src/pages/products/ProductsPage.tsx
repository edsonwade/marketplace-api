import { useState } from 'react';
import { Plus, Search, Edit2, Trash2, ShoppingCart as CartIcon, Package, Filter, X } from 'lucide-react';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';
import { Alert } from '../../components/ui/Alert';
import { Spinner } from '../../components/ui/Spinner';
import { Table, TableHead, TableBody, TableRow, TableHeadCell, TableCell } from '../../components/ui/Table';
import { usePaginatedData } from '../../hooks/usePaginatedData';
import { Paginator } from '../../hooks/Paginator';
import { useProducts, useCreateProduct, useDeleteProduct, useUpdateProduct } from '../../services';
import { useAddItemToCart } from '../../services/cart/service';
import { useAuthStore } from '../../store';
import type { Product } from '../../api/types';

type StatusFilter = 'all' | 'in-stock' | 'low-stock' | 'out-of-stock';

export const ProductsPage = () => {
  const [isAdding,     setIsAdding]     = useState(false);
  const [newProduct,   setNewProduct]   = useState({ name: '', quantity: 0, price: 0 });
  const [editingId,    setEditingId]    = useState<number | null>(null);
  const [editQuantity, setEditQuantity] = useState(0);
  const [editPrice,    setEditPrice]    = useState(0);
  const [deleteId,     setDeleteId]     = useState<number | null>(null);
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('all');

  const { data: products, isLoading, error } = useProducts();
  const createProduct  = useCreateProduct();
  const deleteProduct  = useDeleteProduct();
  const updateProduct  = useUpdateProduct();
  const addToCart      = useAddItemToCart();
  const { user }       = useAuthStore();
  const customerId     = user?.customerId ? Number(user.customerId) : undefined;
  const [cartMsg, setCartMsg] = useState<{ text: string; ok: boolean } | null>(null);

  // Apply status filter before pagination
  const filtered = (products ?? []).filter((p) => {
    if (statusFilter === 'in-stock')    return p.quantity >= 10;
    if (statusFilter === 'low-stock')   return p.quantity > 0 && p.quantity < 10;
    if (statusFilter === 'out-of-stock') return p.quantity === 0;
    return true;
  });

  const { items, query, setQuery, page, setPage, totalPages, totalItems, pageSize } =
    usePaginatedData<Product>({
      data: filtered,
      pageSize: 8,
      filterFn: (p, q) => p.name.toLowerCase().includes(q) || String(p.productId).includes(q),
    });

  const handleCreate = async () => {
    if (!newProduct.name.trim()) return;
    await createProduct.mutateAsync({ name: newProduct.name, quantity: newProduct.quantity, price: newProduct.price });
    setNewProduct({ name: '', quantity: 0, price: 0 });
    setIsAdding(false);
  };

  const handleUpdate = async (product: Product) => {
    await updateProduct.mutateAsync({ id: product.productId, data: { name: product.name, quantity: editQuantity, price: editPrice }, version: product.version });
    setEditingId(null);
  };

  const handleDelete = async (id: number) => {
    await deleteProduct.mutateAsync(id);
    setDeleteId(null);
  };

  const handleAddToCart = async (product: Product) => {
    if (!customerId) { setCartMsg({ text: 'Please log in to add items to cart.', ok: false }); return; }
    if (product.quantity === 0) { setCartMsg({ text: `${product.name} is out of stock.`, ok: false }); return; }
    try {
      await addToCart.mutateAsync({ customerId, productId: product.productId, quantity: 1 });
      setCartMsg({ text: `${product.name} added to cart!`, ok: true });
      setTimeout(() => setCartMsg(null), 2500);
    } catch {
      setCartMsg({ text: 'Failed to add to cart.', ok: false });
    }
  };

  if (isLoading) return <div className="py-16 flex justify-center"><Spinner size="lg" /></div>;
  if (error)     return <Alert variant="error" message="Failed to load products" className="max-w-lg" />;

  const STATUS_FILTERS: { key: StatusFilter; label: string }[] = [
    { key: 'all',          label: 'All' },
    { key: 'in-stock',     label: 'In Stock' },
    { key: 'low-stock',    label: 'Low Stock' },
    { key: 'out-of-stock', label: 'Out of Stock' },
  ];

  return (
    <div className="space-y-5 max-w-7xl">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white">Products</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-0.5">{products?.length ?? 0} total products</p>
        </div>
        <Button onClick={() => setIsAdding(true)} className="self-start sm:self-auto">
          <Plus className="h-4 w-4" aria-hidden="true" /> Add Product
        </Button>
      </div>

      {/* Add form */}
      {cartMsg && (
        <Alert
          variant={cartMsg.ok ? 'success' : 'error'}
          message={cartMsg.text}
          onClose={() => setCartMsg(null)}
        />
      )}

      {isAdding && (
        <Card>
          <div className="px-5 py-3.5 border-b border-slate-200 dark:border-slate-700 flex items-center justify-between">
            <h2 className="text-sm font-semibold text-slate-800 dark:text-slate-200">New Product</h2>
            <button type="button" aria-label="Close" onClick={() => setIsAdding(false)} className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-200">
              <X className="h-4 w-4" aria-hidden="true" />
            </button>
          </div>
          <div className="px-5 py-4 flex flex-col sm:flex-row gap-3 items-end">
            <Input label="Product Name" value={newProduct.name} onChange={(e) => setNewProduct({ ...newProduct, name: e.target.value })} placeholder="e.g., Wireless Headphones" className="sm:max-w-xs" />
            <Input label="Initial Qty" type="number" min={0} value={newProduct.quantity} onChange={(e) => setNewProduct({ ...newProduct, quantity: parseInt(e.target.value) || 0 })} className="sm:w-24" />
            <Input label="Price ($)" type="number" min={0} step={0.01} value={newProduct.price} onChange={(e) => setNewProduct({ ...newProduct, price: parseFloat(e.target.value) || 0 })} className="sm:w-28" />
            <div className="flex gap-2">
              <Button onClick={handleCreate} isLoading={createProduct.isPending}>Save</Button>
              <Button variant="outline" onClick={() => setIsAdding(false)}>Cancel</Button>
            </div>
          </div>
        </Card>
      )}

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3">
        {/* Search */}
        <div className="relative flex-1 max-w-xs">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400 pointer-events-none" aria-hidden="true" />
          <input type="search" placeholder="Search products…" value={query} onChange={(e) => setQuery(e.target.value)}
            className="w-full pl-9 pr-3 py-2 text-sm border border-slate-300 dark:border-slate-600 rounded-lg bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
            aria-label="Search products" />
        </div>

        {/* Status filter pills */}
        <div className="flex items-center gap-1.5 flex-wrap" role="group" aria-label="Filter by status">
          <Filter className="h-4 w-4 text-slate-400 shrink-0" aria-hidden="true" />
          {STATUS_FILTERS.map(({ key, label }) => (
            <button
              key={key}
              type="button"
              onClick={() => { setStatusFilter(key); }}
              className={`px-3 py-1 text-xs font-medium rounded-full border transition-colors
                ${statusFilter === key
                  ? 'bg-blue-600 text-white border-blue-600'
                  : 'bg-white dark:bg-slate-800 text-slate-600 dark:text-slate-300 border-slate-300 dark:border-slate-600 hover:border-blue-400'}`}
            >
              {label}
            </button>
          ))}
        </div>
      </div>

      {/* Table */}
      <div>
        <Table>
          <TableHead>
            <TableRow>
              <TableHeadCell>ID</TableHeadCell>
              <TableHeadCell>Product</TableHeadCell>
              <TableHeadCell>Price</TableHeadCell>
              <TableHeadCell>Quantity</TableHeadCell>
              <TableHeadCell>Status</TableHeadCell>
              <TableHeadCell className="text-right">Actions</TableHeadCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {items.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="text-center py-10 text-slate-400 dark:text-slate-500">
                  <Package className="h-8 w-8 mx-auto mb-2 opacity-30" aria-hidden="true" />
                  {query ? 'No products match your search' : 'No products'}
                </TableCell>
              </TableRow>
            ) : items.map((product) => (
              <TableRow key={product.productId}>
                <TableCell className="font-mono text-xs text-slate-400 dark:text-slate-500">#{product.productId}</TableCell>
                <TableCell className="font-semibold text-slate-800 dark:text-slate-100">{product.name}</TableCell>
                <TableCell>
                  {editingId === product.productId
                    ? <Input type="number" min={0} step={0.01} value={editPrice} onChange={(e) => setEditPrice(parseFloat(e.target.value) || 0)} className="w-24" aria-label="Edit price" />
                    : <span className="font-medium text-slate-700 dark:text-slate-200">${Number(product.price ?? 0).toFixed(2)}</span>
                  }
                </TableCell>
                <TableCell>
                  {editingId === product.productId
                    ? <Input type="number" min={0} value={editQuantity} onChange={(e) => setEditQuantity(parseInt(e.target.value) || 0)} className="w-24" aria-label="Edit quantity" />
                    : <span className="font-medium text-slate-700 dark:text-slate-200">{product.quantity}</span>
                  }
                </TableCell>
                <TableCell>
                  {product.quantity === 0  ? <Badge variant="danger"  dot>Out of Stock</Badge>
                  : product.quantity < 10  ? <Badge variant="warning" dot>Low Stock</Badge>
                                           : <Badge variant="success" dot>In Stock</Badge>}
                </TableCell>
                <TableCell>
                  <div className="flex items-center justify-end gap-1.5">
                    {editingId === product.productId ? (
                      <>
                        <Button size="sm" onClick={() => handleUpdate(product)} isLoading={updateProduct.isPending}>Save</Button>
                        <Button size="sm" variant="outline" onClick={() => setEditingId(null)}>Cancel</Button>
                      </>
                    ) : deleteId === product.productId ? (
                      <>
                        <span className="text-xs text-slate-500 dark:text-slate-400 mr-1">Delete?</span>
                        <Button size="sm" variant="danger" onClick={() => handleDelete(product.productId)} isLoading={deleteProduct.isPending}>Confirm</Button>
                        <Button size="sm" variant="outline" onClick={() => setDeleteId(null)}>Cancel</Button>
                      </>
                    ) : (
                      <>
                        <Button size="sm" variant="outline" aria-label={`Edit ${product.name}`} onClick={() => { setEditingId(product.productId); setEditQuantity(product.quantity); setEditPrice(Number(product.price ?? 0)); }}>
                          <Edit2 className="h-3.5 w-3.5" aria-hidden="true" />
                        </Button>
                        <Button size="sm" variant="ghost" aria-label={`Add to cart`} onClick={() => handleAddToCart(product)} disabled={product.quantity === 0} className="text-blue-600 dark:text-blue-400 hover:bg-blue-50 dark:hover:bg-blue-900/30">
                          <CartIcon className="h-3.5 w-3.5" aria-hidden="true" />
                        </Button>
                        <Button size="sm" variant="ghost" aria-label={`Delete ${product.name}`} onClick={() => setDeleteId(product.productId)} className="text-red-500 hover:bg-red-50 dark:hover:bg-red-900/30">
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
