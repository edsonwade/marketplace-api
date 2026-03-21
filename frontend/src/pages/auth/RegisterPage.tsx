import { useForm } from 'react-hook-form';
import { useNavigate, Link } from 'react-router-dom';
import { Eye, EyeOff, Package, UserPlus, CheckCircle2, Circle } from 'lucide-react';
import { useState } from 'react';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { Alert } from '../../components/ui/Alert';
import { useRegister } from '../../services';

interface RegisterForm {
  email: string;
  password: string;
  confirmPassword: string;
}

const RULES = [
  { label: 'At least 8 characters',    test: (p: string) => p.length >= 8 },
  { label: 'One uppercase letter',      test: (p: string) => /[A-Z]/.test(p) },
  { label: 'One lowercase letter',      test: (p: string) => /[a-z]/.test(p) },
  { label: 'One number',               test: (p: string) => /\d/.test(p) },
  { label: 'One special character',    test: (p: string) => /[!@#$%^&*(),.?":{}|<>]/.test(p) },
];

const getRegisterError = (error: unknown): string => {
  if (!error) return '';
  if (typeof error === 'object' && error !== null && 'response' in error) {
    const resp = (error as { response?: { status?: number; data?: { message?: string } } }).response;
    if (resp?.status === 409) return 'An account with this email already exists.';
    return resp?.data?.message || 'Registration failed. Please try again.';
  }
  return 'Registration failed. Please try again.';
};

export const RegisterPage = () => {
  const navigate = useNavigate();
  const registerMutation = useRegister();
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const { register, handleSubmit, watch, formState: { errors } } = useForm<RegisterForm>();
  const password = watch('password', '');

  const strengthPassed = RULES.filter((r) => r.test(password)).length;
  const strengthColor =
    strengthPassed <= 2 ? 'bg-red-400' :
    strengthPassed <= 3 ? 'bg-amber-400' :
    strengthPassed <= 4 ? 'bg-blue-400' : 'bg-emerald-500';

  const onSubmit = async (data: RegisterForm) => {
    try {
      await registerMutation.mutateAsync({ email: data.email, password: data.password });
      navigate('/login');
    } catch {
      /* handled below */
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 p-4 py-8">
      <div className="w-full max-w-[420px] bg-white rounded-2xl shadow-2xl overflow-hidden">

        {/* Top accent bar */}
        <div className="h-1 bg-gradient-to-r from-blue-500 via-blue-400 to-blue-600" />

        <div className="px-8 py-8">

          {/* Brand */}
          <div className="flex flex-col items-center mb-8">
            <div className="flex items-center justify-center w-12 h-12 rounded-xl bg-blue-600 mb-3 shadow-md">
              <Package className="h-6 w-6 text-white" aria-hidden="true" />
            </div>
            <h1 className="text-xl font-bold text-slate-900">Create account</h1>
            <p className="text-sm text-slate-500 mt-1">Join the marketplace platform</p>
          </div>

          {registerMutation.isError && (
            <Alert
              variant="error"
              message={getRegisterError(registerMutation.error)}
              className="mb-5"
            />
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
            <Input
              label="Email address"
              type="email"
              placeholder="you@company.com"
              autoComplete="email"
              error={errors.email?.message}
              {...register('email', {
                required: 'Email is required',
                pattern: {
                  value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                  message: 'Please enter a valid email',
                },
              })}
            />

            {/* Password + strength */}
            <div>
              <div className="relative">
                <Input
                  label="Password"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Create a strong password"
                  autoComplete="new-password"
                  error={errors.password?.message}
                  {...register('password', {
                    required: 'Password is required',
                    minLength: { value: 8, message: 'At least 8 characters required' },
                  })}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((v) => !v)}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                  className="absolute right-3 top-[30px] text-slate-400 hover:text-slate-600 transition-colors"
                >
                  {showPassword ? <EyeOff className="h-4 w-4" aria-hidden="true" /> : <Eye className="h-4 w-4" aria-hidden="true" />}
                </button>
              </div>

              {/* Strength bar */}
              {password.length > 0 && (
                <div className="mt-2">
                  <div className="flex gap-1 mb-2">
                    {[1, 2, 3, 4, 5].map((n) => (
                      <div
                        key={n}
                        className={`h-1 flex-1 rounded-full transition-all duration-300 ${n <= strengthPassed ? strengthColor : 'bg-slate-200'}`}
                      />
                    ))}
                  </div>
                  <div className="grid grid-cols-2 gap-x-4 gap-y-0.5">
                    {RULES.map((rule) => {
                      const ok = rule.test(password);
                      return (
                        <div key={rule.label} className={`flex items-center gap-1.5 text-[11px] ${ok ? 'text-emerald-600' : 'text-slate-400'}`}>
                          {ok
                            ? <CheckCircle2 aria-hidden="true" className="h-3 w-3 shrink-0" />
                            : <Circle aria-hidden="true" className="h-3 w-3 shrink-0" />}
                          {rule.label}
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}
            </div>

            <div className="relative">
              <Input
                label="Confirm password"
                type={showConfirm ? 'text' : 'password'}
                placeholder="Re-enter your password"
                autoComplete="new-password"
                error={errors.confirmPassword?.message}
                {...register('confirmPassword', {
                  required: 'Please confirm your password',
                  validate: (v) => v === password || 'Passwords do not match',
                })}
              />
              <button
                type="button"
                onClick={() => setShowConfirm((v) => !v)}
                aria-label={showConfirm ? 'Hide password' : 'Show password'}
                className="absolute right-3 top-[30px] text-slate-400 hover:text-slate-600 transition-colors"
              >
                {showConfirm ? <EyeOff className="h-4 w-4" aria-hidden="true" /> : <Eye className="h-4 w-4" aria-hidden="true" />}
              </button>
            </div>

            <Button
              type="submit"
              className="w-full mt-2"
              size="lg"
              isLoading={registerMutation.isPending}
              disabled={registerMutation.isPending}
            >
              <UserPlus className="h-4 w-4" aria-hidden="true" />
              Create account
            </Button>
          </form>

          <p className="text-center text-sm text-slate-500 mt-6">
            Already have an account?{' '}
            <Link to="/login" className="font-semibold text-blue-600 hover:text-blue-700 hover:underline">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};
