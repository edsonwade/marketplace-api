// Paginator — JSX component, must be .tsx
// The usePaginatedData hook lives in usePaginatedData.ts

interface PaginatorProps {
  page: number;
  totalPages: number;
  totalItems: number;
  pageSize: number;
  onPage: (p: number) => void;
}

export const Paginator = ({ page, totalPages, totalItems, pageSize, onPage }: PaginatorProps) => {
  if (totalPages <= 1) return null;

  const from = (page - 1) * pageSize + 1;
  const to   = Math.min(page * pageSize, totalItems);

  const pages: (number | '...')[] = [];
  if (totalPages <= 7) {
    for (let i = 1; i <= totalPages; i++) pages.push(i);
  } else {
    pages.push(1);
    if (page > 3)              pages.push('...');
    for (let i = Math.max(2, page - 1); i <= Math.min(totalPages - 1, page + 1); i++) pages.push(i);
    if (page < totalPages - 2) pages.push('...');
    pages.push(totalPages);
  }

  const btnBase =
    'px-2 py-1 text-xs rounded border transition-colors disabled:opacity-40 disabled:cursor-not-allowed';
  const btnIdle =
    'border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800';

  return (
    <div className="flex items-center justify-between px-1 pt-3">
      <p className="text-xs text-slate-500 dark:text-slate-400">
        Showing{' '}
        <span className="font-medium text-slate-700 dark:text-slate-200">{from}&ndash;{to}</span>
        {' '}of{' '}
        <span className="font-medium text-slate-700 dark:text-slate-200">{totalItems}</span>
      </p>

      <nav aria-label="Pagination" className="flex items-center gap-1">
        <button
          type="button"
          onClick={() => onPage(page - 1)}
          disabled={page === 1}
          aria-label="Previous page"
          className={`${btnBase} ${btnIdle}`}
        >
          {'<'}
        </button>

        {pages.map((p, i) =>
          p === '...' ? (
            <span key={`ellipsis-${i}`} className="px-1.5 text-xs text-slate-400 select-none">
              &hellip;
            </span>
          ) : (
            <button
              key={p}
              type="button"
              onClick={() => onPage(p as number)}
              aria-current={p === page ? 'page' : undefined}
              className={`min-w-[28px] h-7 text-xs rounded border transition-colors ${
                p === page
                  ? 'bg-blue-600 text-white border-blue-600 font-semibold'
                  : btnIdle
              }`}
            >
              {p}
            </button>
          )
        )}

        <button
          type="button"
          onClick={() => onPage(page + 1)}
          disabled={page === totalPages}
          aria-label="Next page"
          className={`${btnBase} ${btnIdle}`}
        >
          {'>'}
        </button>
      </nav>
    </div>
  );
};
