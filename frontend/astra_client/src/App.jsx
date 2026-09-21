import { lazy, Suspense, useEffect } from 'react';
import { useDispatch } from 'react-redux';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { logout } from './store/authSlice';
import ErrorBoundary from './components/common/ErrorBoundary';
import { ToastProvider } from './context/ToastContext';

const Home = lazy(() => import('./pages/Home'));
const ProductListing = lazy(() => import('./pages/ProductListing'));
const ProductDetails = lazy(() => import('./pages/ProductDetails'));
const Cart = lazy(() => import('./pages/Cart'));
const Checkout = lazy(() => import('./pages/Checkout'));
const Orders = lazy(() => import('./pages/Orders'));
const OrderDetails = lazy(() => import('./pages/OrderDetails'));
const Wishlist = lazy(() => import('./pages/Wishlist'));
const AdminDashboard = lazy(() => import('./pages/AdminDashboard'));
const Notifications = lazy(() => import('./pages/Notifications'));
const Recommendations = lazy(() => import('./pages/Recommendations'));
const Tracking = lazy(() => import('./pages/Tracking'));
const Returns = lazy(() => import('./pages/Returns'));
const NotFound = lazy(() => import('./pages/NotFound'));

function LoadingScreen() {
  return <main className="loading-screen" aria-busy="true"><div className="spinner"/><p>Loading ASTRA…</p></main>;
}

export default function App() {
  const dispatch = useDispatch();
  useEffect(() => {
    const handler = () => dispatch(logout());
    window.addEventListener('astra:session-expired', handler);
    return () => window.removeEventListener('astra:session-expired', handler);
  }, [dispatch]);

  return (
    <ErrorBoundary>
      <ToastProvider>
        <BrowserRouter>
          <Suspense fallback={<LoadingScreen />}>
            <Routes>
              <Route path="/" element={<Home />} />
              <Route path="/products" element={<ProductListing />} />
              <Route path="/products/:slug" element={<ProductDetails />} />
              <Route path="/cart" element={<Cart />} />
              <Route path="/checkout" element={<Checkout />} />
              <Route path="/orders" element={<Orders />} />
              <Route path="/orders/:orderNumber" element={<OrderDetails />} />
              <Route path="/wishlist" element={<Wishlist />} />
              <Route path="/admin" element={<AdminDashboard />} />
              <Route path="/notifications" element={<Notifications />} />
              <Route path="/recommendations" element={<Recommendations />} />
              <Route path="/tracking" element={<Tracking />} />
              <Route path="/returns" element={<Returns />} />
              <Route path="*" element={<NotFound />} />
            </Routes>
          </Suspense>
        </BrowserRouter>
      </ToastProvider>
    </ErrorBoundary>
  );
}
