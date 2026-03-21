interface SpinnerProps {
  size?: 'xs' | 'sm' | 'md' | 'lg';
  className?: string;
  label?: string;
}

export const Spinner = ({ size = 'md', className = '', label = 'Loading…' }: SpinnerProps) => {
  const sizes = {
    xs: 'h-3 w-3 border',
    sm: 'h-4 w-4 border-2',
    md: 'h-7 w-7 border-2',
    lg: 'h-10 w-10 border-[3px]',
  };

  return (
    <div role="status" aria-label={label} className={`flex items-center justify-center ${className}`}>
      <div
        className={`
          ${sizes[size]} animate-spin rounded-full
          border-slate-200 border-t-blue-600
        `}
      />
      <span className="sr-only">{label}</span>
    </div>
  );
};

export const FullPageSpinner = () => (
  <div className="flex items-center justify-center min-h-[400px]">
    <Spinner size="lg" />
  </div>
);
