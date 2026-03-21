import { type TableHTMLAttributes, type TdHTMLAttributes, type ThHTMLAttributes, forwardRef } from 'react';

export const Table = forwardRef<HTMLTableElement, TableHTMLAttributes<HTMLTableElement>>(
  ({ className = '', ...props }, ref) => (
    <div className="overflow-x-auto rounded-xl border border-slate-200 dark:border-slate-700">
      <table ref={ref} className={`min-w-full divide-y divide-slate-200 dark:divide-slate-700 text-sm ${className}`} {...props} />
    </div>
  )
);
Table.displayName = 'Table';

export const TableHead = forwardRef<HTMLTableSectionElement, TableHTMLAttributes<HTMLTableSectionElement>>(
  ({ className = '', ...props }, ref) => (
    <thead ref={ref} className={`bg-slate-100 dark:bg-slate-800 ${className}`} {...props} />
  )
);
TableHead.displayName = 'TableHead';

export const TableBody = forwardRef<HTMLTableSectionElement, TableHTMLAttributes<HTMLTableSectionElement>>(
  ({ className = '', ...props }, ref) => (
    <tbody ref={ref} className={`bg-white dark:bg-slate-900 divide-y divide-slate-100 dark:divide-slate-800 ${className}`} {...props} />
  )
);
TableBody.displayName = 'TableBody';

export const TableRow = forwardRef<HTMLTableRowElement, TableHTMLAttributes<HTMLTableRowElement>>(
  ({ className = '', ...props }, ref) => (
    <tr ref={ref} className={`transition-colors hover:bg-slate-50 dark:hover:bg-slate-800/60 ${className}`} {...props} />
  )
);
TableRow.displayName = 'TableRow';

export const TableHeadCell = forwardRef<HTMLTableCellElement, ThHTMLAttributes<HTMLTableCellElement>>(
  ({ className = '', ...props }, ref) => (
    <th ref={ref} scope="col"
      className={`px-5 py-3 text-left text-[11px] font-semibold text-slate-600 dark:text-slate-300 uppercase tracking-wider whitespace-nowrap ${className}`}
      {...props}
    />
  )
);
TableHeadCell.displayName = 'TableHeadCell';

export const TableCell = forwardRef<HTMLTableCellElement, TdHTMLAttributes<HTMLTableCellElement>>(
  ({ className = '', ...props }, ref) => (
    <td ref={ref} className={`px-5 py-3.5 text-slate-700 dark:text-slate-200 whitespace-nowrap ${className}`} {...props} />
  )
);
TableCell.displayName = 'TableCell';
