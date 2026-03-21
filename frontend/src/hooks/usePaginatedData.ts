import { useState, useMemo } from 'react';

export interface PaginationOptions<T> {
  data: T[] | undefined;
  pageSize?: number;
  filterFn?: (item: T, query: string) => boolean;
}

export function usePaginatedData<T>({ data = [], pageSize = 10, filterFn }: PaginationOptions<T>) {
  const [page,  setPage]  = useState(1);
  const [query, setQuery] = useState('');

  const filtered = useMemo(() => {
    if (!query || !filterFn) return data;
    return data.filter((item) => filterFn(item, query.toLowerCase()));
  }, [data, query, filterFn]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
  const safePage   = Math.min(page, totalPages);

  const paginated = useMemo(
    () => filtered.slice((safePage - 1) * pageSize, safePage * pageSize),
    [filtered, safePage, pageSize]
  );

  const handleSearch = (q: string) => { setQuery(q); setPage(1); };

  return {
    items: paginated,
    query,
    setQuery: handleSearch,
    page: safePage,
    setPage,
    totalPages,
    totalItems: filtered.length,
    pageSize,
  };
}
