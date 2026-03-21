import { useState } from 'react';
import { User, LogOut, Shield, Info, Edit2, Check, X } from 'lucide-react';
import { Card, CardBody, CardHeader } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Alert } from '../../components/ui/Alert';
import { Spinner } from '../../components/ui/Spinner';
import { useCurrentUser, useUpdateCustomer } from '../../services/customer/service';
import { useAuthStore } from '../../store';
import { useLogout } from '../../services';
import { useNavigate } from 'react-router-dom';

export const SettingsPage = () => {
  const { data: user, isLoading, error } = useCurrentUser();
  const [isEditing,   setIsEditing]   = useState(false);
  const [formData,    setFormData]    = useState({ name: '', email: '', address: '' });
  const [saveError,   setSaveError]   = useState<string | null>(null);
  const [saveSuccess, setSaveSuccess] = useState(false);
  const navigate = useNavigate();
  const updateCustomer = useUpdateCustomer();
  const logout = useLogout();
  const { clearAuth } = useAuthStore();

  const handleEdit = () => {
    if (!user) return;
    setFormData({ name: user.name, email: user.email, address: user.address });
    setSaveError(null); setSaveSuccess(false); setIsEditing(true);
  };

  const handleSave = async () => {
    if (!formData.name.trim())    { setSaveError('Name is required');    return; }
    if (!formData.email.trim())   { setSaveError('Email is required');   return; }
    if (!formData.address.trim()) { setSaveError('Address is required'); return; }
    try {
      setSaveError(null); setSaveSuccess(false);
      await updateCustomer.mutateAsync({ id: user!.customerId, data: formData });
      setSaveSuccess(true); setIsEditing(false);
    } catch { setSaveError('Failed to update profile.'); }
  };

  const handleLogout = async () => {
    try { await logout.mutateAsync(); } catch {}
    clearAuth(); navigate('/login', { replace: true });
  };

  const initials = user?.name ? user.name.split(' ').map((n) => n[0]).join('').toUpperCase().slice(0, 2) : '?';

  if (isLoading) return <div className="py-16 flex justify-center"><Spinner size="lg" /></div>;
  if (error)     return <Alert variant="error" message="Failed to load profile" className="max-w-lg" />;

  return (
    <div className="space-y-6 max-w-2xl">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-white">Settings</h1>
        <p className="text-sm text-slate-500 dark:text-slate-400 mt-0.5">Manage your profile and preferences</p>
      </div>

      {/* Profile */}
      <Card>
        <CardHeader>
          <div className="flex items-center gap-2 text-sm font-semibold text-slate-700 dark:text-slate-200">
            <User className="h-4 w-4 text-slate-400" aria-hidden="true" /> Profile
          </div>
          {!isEditing && <Button size="sm" variant="outline" onClick={handleEdit}><Edit2 className="h-3.5 w-3.5" aria-hidden="true" /> Edit</Button>}
        </CardHeader>
        <CardBody className="space-y-5">
          <div className="flex items-center gap-4">
            <div aria-hidden="true" className="w-14 h-14 rounded-2xl bg-blue-600 flex items-center justify-center text-white text-lg font-bold shrink-0 shadow">
              {initials}
            </div>
            <div>
              <p className="font-semibold text-slate-800 dark:text-slate-100">{user?.name}</p>
              <p className="text-sm text-slate-500 dark:text-slate-400 truncate">{user?.email}</p>
            </div>
          </div>

          {saveSuccess && <Alert variant="success" message="Profile updated successfully!" />}

          {isEditing ? (
            <div className="space-y-4">
              {saveError && <Alert variant="error" message={saveError} />}
              <Input label="Full name" value={formData.name}    onChange={(e) => setFormData({ ...formData, name: e.target.value })} />
              <Input label="Email"     type="email" value={formData.email}   onChange={(e) => setFormData({ ...formData, email: e.target.value })} />
              <Input label="Address"  value={formData.address} onChange={(e) => setFormData({ ...formData, address: e.target.value })} />
              <div className="flex gap-3 pt-1">
                <Button onClick={handleSave} isLoading={updateCustomer.isPending} className="flex-1"><Check className="h-4 w-4" aria-hidden="true" /> Save</Button>
                <Button variant="outline" onClick={() => setIsEditing(false)} className="flex-1"><X className="h-4 w-4" aria-hidden="true" /> Cancel</Button>
              </div>
            </div>
          ) : (
            <dl className="space-y-3">
              {[{ label: 'Name', value: user?.name }, { label: 'Email', value: user?.email }, { label: 'Address', value: user?.address || '—' }].map(({ label, value }) => (
                <div key={label} className="flex gap-4">
                  <dt className="w-20 shrink-0 text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide pt-0.5">{label}</dt>
                  <dd className="text-sm text-slate-800 dark:text-slate-200 flex-1">{value}</dd>
                </div>
              ))}
            </dl>
          )}
        </CardBody>
      </Card>

      {/* Security */}
      <Card>
        <CardHeader>
          <div className="flex items-center gap-2 text-sm font-semibold text-slate-700 dark:text-slate-200">
            <Shield className="h-4 w-4 text-slate-400" aria-hidden="true" /> Security
          </div>
        </CardHeader>
        <CardBody className="space-y-4">
          <p className="text-sm text-slate-600 dark:text-slate-300">Session protected with JWT. Access tokens refresh automatically.</p>
          <div className="flex items-center gap-3 p-3 bg-emerald-50 dark:bg-emerald-900/20 rounded-lg border border-emerald-100 dark:border-emerald-800">
            <div className="w-2 h-2 rounded-full bg-emerald-500" aria-hidden="true" />
            <span className="text-sm text-emerald-700 dark:text-emerald-400 font-medium">Session active</span>
          </div>
          <Button variant="danger" onClick={handleLogout} isLoading={logout.isPending}>
            <LogOut className="h-4 w-4" aria-hidden="true" /> Sign out
          </Button>
        </CardBody>
      </Card>

      {/* About */}
      <Card>
        <CardHeader>
          <div className="flex items-center gap-2 text-sm font-semibold text-slate-700 dark:text-slate-200">
            <Info className="h-4 w-4 text-slate-400" aria-hidden="true" /> About
          </div>
        </CardHeader>
        <CardBody>
          <dl className="space-y-2">
            {[{ label: 'Application', value: 'Marketplace API' }, { label: 'Version', value: '1.0.0' }, { label: 'Frontend', value: 'React 19 + Vite + Tailwind v4' }, { label: 'Backend', value: 'Spring Boot (Java)' }].map(({ label, value }) => (
              <div key={label} className="flex justify-between items-center text-sm py-1.5 border-b border-slate-100 dark:border-slate-700 last:border-0">
                <dt className="text-slate-500 dark:text-slate-400">{label}</dt>
                <dd className="font-medium text-slate-800 dark:text-slate-200">{value}</dd>
              </div>
            ))}
          </dl>
        </CardBody>
      </Card>
    </div>
  );
};
