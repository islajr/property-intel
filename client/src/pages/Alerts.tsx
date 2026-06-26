import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { alerts } from '../api';
import AlertCard from '../components/alerts/AlertCard';
import AlertForm from '../components/alerts/AlertForm';
import Button from '../components/primitives/Button';
import Skeleton from '../components/primitives/Skeleton';
import { Bell, Plus, AlertCircle } from 'lucide-react';
import { useToast } from '../components/toast/ToastProvider';

export default function Alerts() {
  const queryClient = useQueryClient();
  const { addToast } = useToast();
  const [showCreateForm, setShowCreateForm] = useState(false);

  // 1. Fetch user alerts list
  const { data: alertsList, isLoading, isError, refetch } = useQuery({
    queryKey: ['alerts'],
    queryFn: () => alerts.list(),
  });

  // 2. Alert Deletion Mutation
  const deleteMutation = useMutation({
    mutationFn: (id: number) => alerts.remove(id),
    onSuccess: () => {
      // Invalidate query to refresh list
      queryClient.invalidateQueries({ queryKey: ['alerts'] });
      addToast('Alert deleted', 'success');
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to delete alert. Please try again.';
      addToast('Unable to delete alert', 'error', msg);
    }
  });

  const handleDeleteAlert = async (id: number) => {
    await deleteMutation.mutateAsync(id);
  };

  const handleCreateSuccess = () => {
    setShowCreateForm(false);
    queryClient.invalidateQueries({ queryKey: ['alerts'] });
    addToast('Alert created', 'success');
  };

  return (
    <div className="alerts-page-container container py-8">
      {/* Header section */}
      <header className="flex justify-between items-center gap-4 mb-8">
        <div>
          <h1 className="page-title text-2xl font-bold tracking-tight text-primary">
            My Alerts
          </h1>
          <p className="text-sm text-secondary mt-1">
            Configure criteria to receive notifications for matching property listings.
          </p>
        </div>

        {!showCreateForm && (
          <Button 
            variant="primary" 
            onClick={() => setShowCreateForm(true)}
            className="flex items-center gap-1.5"
          >
            <Plus size={16} />
            New Alert
          </Button>
        )}
      </header>

      {/* Collapsible Create Form */}
      {showCreateForm && (
        <AlertForm 
          onSuccess={handleCreateSuccess} 
          onCancel={() => setShowCreateForm(false)} 
        />
      )}

      {/* Main Alerts List Content */}
      {isLoading ? (
        <div className="flex flex-col gap-4">
          <Skeleton height={140} borderRadius={8} />
          <Skeleton height={140} borderRadius={8} />
          <Skeleton height={140} borderRadius={8} />
        </div>
      ) : isError ? (
        <div className="error-banner">
          <AlertCircle size={20} className="text-secondary" />
          <div>
            <p className="text-md">Connection error</p>
            <p className="text-sm text-secondary">
              Unable to load your alerts. Check your network connection.
            </p>
            <Button variant="secondary" size="sm" onClick={() => refetch()} style={{ marginTop: 'var(--space-2)' }}>
              Retry
            </Button>
          </div>
        </div>
      ) : !alertsList || alertsList.length === 0 ? (
        <div className="alerts-empty-state text-center py-16 bg-raised border border-default border-dashed rounded-lg p-8">
          <div className="inline-flex items-center justify-center p-3 rounded-full bg-subtle text-amber mb-4 border border-strong">
            <Bell size={24} />
          </div>
          <h3 className="text-md font-bold text-primary mb-1">No alerts configured</h3>
          <p className="text-sm text-secondary max-w-sm mx-auto mb-6">
            Get instant notifications when properties in Ajah, Lekki, or Gbagada drop in price or match your criteria.
          </p>
          <Button variant="primary" onClick={() => setShowCreateForm(true)}>
            <Plus size={16} className="mr-2" />
            Create your first alert
          </Button>
        </div>
      ) : (
        <div className="alerts-grid grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {alertsList.map((alert) => (
            <AlertCard 
              key={alert.id} 
              alert={alert} 
              onDelete={handleDeleteAlert} 
            />
          ))}
        </div>
      )}
    </div>
  );
}
