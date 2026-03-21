import { type TableHTMLAttributes, type TdHTMLAttributes, type ThHTMLAttributes, forwardRef } from 'react';

/* ---- Wrapper ---- */
export const Table = forwardRef<HTMLTableElement, TableHTMLAttributes<HTMLTableElement>>(
  ({ className = '', ...props }, ref) => (
    <div className="overflow-x-auto rounded-xl border border-slate-200">
      <table
        ref={ref}
        className={`min-w-full divide-y divide-slate-100 text-sm ${className}`}
        {...props}
      />
    </div>
  )
);
Table.displayName = 'Table';

/* ---- Head ---- */
export const TableHead = forwardRef<HTMLTableSectionElement, TableHTMLAttributes<HTMLTableSectionElement>>(
  ({ className = '', ...props }, ref) => (
    <thead ref={ref} className={`bg-slate-50 ${className}`} {...props} />
  )
);
TableHead.displayName = 'TableHead';

/* ---- Body ---- */
export const TableBody = forwardRef<HTMLTableSectionElement, TableHTMLAttributes<HTMLTableSectionElement>>(
  ({ className = '', ...props }, ref) => (
    <tbody ref={ref} className={`bg-white divide-y divide-slate-100 ${className}`} {...props} />
  )
);
TableBody.displayName = 'TableBody';

/* ---- Row ---- */
export const TableRow = forwardRef<HTMLTableRowElement, TableHTMLAttributes<HTMLTableRowElement>>(
  ({ className = '', ...props }, ref) => (
    <tr ref={ref} className={`transition-colors hover:bg-slate-50/70 ${className}`} {...props} />
  )
);
TableRow.displayName = 'TableRow';

/* ---- Head Cell ---- */
export const TableHeadCell = forwardRef<HTMLTableCellElement, ThHTMLAttributes<HTMLTableCellElement>>(
  ({ className = '', ...props }, ref) => (
    <th
      ref={ref}
      scope="col"
      className={`px-5 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-wider whitespace-nowrap ${className}`}
      {...props}
    />
  )
);
TableHeadCell.displayName = 'TableHeadCell';

/* ---- Data Cell ---- */
export const TableCell = forwardRef<HTMLTableCellElement, TdHTMLAttributes<HTMLTableCellElement>>(
  ({ className = '', ...props }, ref) => (
    <td
      ref={ref}
      className={`px-5 py-3.5 text-slate-700 whitespace-nowrap ${className}`}
      {...props}
    />
  )
);
TableCell.displayName = 'TableCell';
