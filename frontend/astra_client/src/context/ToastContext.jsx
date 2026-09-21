import { createContext, useCallback, useContext, useMemo, useState } from 'react';

const ToastContext = createContext(null);

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);
  const push = useCallback((message, type = 'info') => {
    const id = crypto?.randomUUID?.() || `${Date.now()}-${Math.random()}`;
    setToasts(current => [...current, { id, message, type }]);
    window.setTimeout(() => setToasts(current => current.filter(t => t.id !== id)), 3200);
  }, []);
  const value = useMemo(() => ({ push, success: m => push(m, 'success'), error: m => push(m, 'error') }), [push]);

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="toast-stack" aria-live="polite">
        {toasts.map(t => <div key={t.id} className={`toast toast-${t.type}`}>{t.message}</div>)}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const context = useContext(ToastContext);
  if (!context) throw new Error('useToast must be used inside ToastProvider');
  return context;
}
