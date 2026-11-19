import { lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import ProtectedRoute from "./ProtectedRoute";

const HomePage = lazy(() => import("../pages/HomePage"));
const LoginPage = lazy(() => import("../pages/auth/LoginPage"));
const RegisterPage = lazy(() => import("../pages/auth/RegisterPage"));
const ForgotPasswordPage = lazy(() => import("../pages/auth/ForgotPasswordPage"));
const VerifyOtpPage = lazy(() => import("../pages/auth/VerifyOtpPage"));
const ResetPasswordPage = lazy(() => import("../pages/auth/ResetPasswordPage"));
const RegisterOtpPage = lazy(() => import("../pages/auth/RegisterOtpPage"));

const ProductReviewsPage = lazy(() => import("../pages/ProductReviewsPage"));
const MyOrdersPage = lazy(() => import("../pages/MyOrdersPage"));
const ProfilePage = lazy(() => import("../pages/ProfilePage"));
const AboutPage = lazy(() => import("../pages/AboutPage"));
const ProductsPage = lazy(() => import("../pages/ProductsPage"));
const CartPage = lazy(() => import("../pages/CartPage"));
const ProductDetailsPage = lazy(() => import("../pages/ProductDetailsPage"));
const CheckoutPage = lazy(() => import("../pages/CheckoutPage"));
const OrderConfirmationPage = lazy(() => import("../pages/OrderConfirmationPage"));
const OrderTrackingPage = lazy(() => import("../pages/OrderTrackingPage"));
const WishlistPage = lazy(() => import("../pages/WishlistPage"));
const AddressesPage = lazy(() => import("../pages/AddressesPage"));
const CompareProductsPage = lazy(() => import("../pages/CompareProductsPage"));
const MyFilesPage = lazy(() => import("../pages/MyFilesPage"));

const AdminOrdersPage = lazy(() => import("../pages/AdminOrdersPage"));
const AdminOrderDetailsPage = lazy(() => import("../pages/AdminOrderDetailsPage"));
const AdminDashboardPage = lazy(() => import("../pages/AdminDashboardPage"));
const AdminCouponsPage = lazy(() => import("../pages/AdminCouponsPage"));
const AdminRegisterPage = lazy(() => import("../pages/AdminRegisterPage"));
const AdminProductsPage = lazy(() => import("../pages/AdminProductsPage"));
const AdminInventoryPage = lazy(() => import("../pages/AdminInventoryPage"));
const AdminCustomersPage = lazy(() => import("../pages/AdminCustomersPage"));
const AdminReviewsPage = lazy(() => import("../pages/AdminReviewsPage"));
const AdminFileManagerPage = lazy(() => import("../pages/AdminFileManagerPage"));
const AdminWarehousePage = lazy(() => import("../pages/AdminWarehousePage"));

const AdminDatabaseActivityPage = lazy(
  () => import("../pages/AdminDatabaseActivityPage")
);

const AdminApiMonitorPage = lazy(
  () => import("../pages/AdminApiMonitorPage")
);

const AdminLayout = lazy(() => import("../layouts/AdminLayout"));
const StorefrontLayout = lazy(() => import("../layouts/StorefrontLayout"));

export default function AppRoutes() {
  return (
    <Suspense
      fallback={
        <main className="flex min-h-[60vh] items-center justify-center bg-[#09090f] text-sm text-gray-400">
          Loading page…
        </main>
      }
    >
      <Routes>
        {/* Customer storefront routes */}
        <Route element={<StorefrontLayout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/about" element={<AboutPage />} />
          <Route path="/products" element={<ProductsPage />} />
          <Route path="/products/:id" element={<ProductDetailsPage />} />
          <Route
            path="/products/:id/reviews"
            element={<ProductReviewsPage />}
          />

          <Route
            path="/cart"
            element={
              <ProtectedRoute>
                <CartPage />
              </ProtectedRoute>
            }
          />

          <Route path="/compare" element={<CompareProductsPage />} />

          <Route
            path="/wishlist"
            element={
              <ProtectedRoute>
                <WishlistPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/checkout"
            element={
              <ProtectedRoute>
                <CheckoutPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/my-files"
            element={
              <ProtectedRoute>
                <MyFilesPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/order-confirmation/:orderNumber"
            element={
              <ProtectedRoute>
                <OrderConfirmationPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/my-orders"
            element={
              <ProtectedRoute>
                <MyOrdersPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/track-order/:orderNumber"
            element={
              <ProtectedRoute>
                <OrderTrackingPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <ProfilePage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/addresses"
            element={
              <ProtectedRoute>
                <AddressesPage />
              </ProtectedRoute>
            }
          />
        </Route>

        {/* Authentication routes */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/verify-otp" element={<VerifyOtpPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route path="/register/verify-otp" element={<RegisterOtpPage />} />

        {/* Admin routes */}
        <Route
          path="/admin"
          element={
            <ProtectedRoute allowedRoles={["ADMIN"]}>
              <AdminLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<AdminDashboardPage />} />

          <Route path="orders" element={<AdminOrdersPage />} />
          <Route
            path="orders/:orderNumber"
            element={<AdminOrderDetailsPage />}
          />

          <Route path="customers" element={<AdminCustomersPage />} />
          <Route path="reviews" element={<AdminReviewsPage />} />
          <Route path="products" element={<AdminProductsPage />} />
          <Route path="inventory" element={<AdminInventoryPage />} />
          <Route path="warehouses" element={<AdminWarehousePage />} />
          <Route path="coupons" element={<AdminCouponsPage />} />
          <Route path="register" element={<AdminRegisterPage />} />

          {/* Admin File Manager */}
          <Route path="files" element={<AdminFileManagerPage />} />

          {/* Astra API Request Monitor */}
          <Route path="api-monitor" element={<AdminApiMonitorPage />} />

          {/* Astra Database Activity Monitor */}
          <Route
            path="database-activity"
            element={<AdminDatabaseActivityPage />}
          />
        </Route>

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  );
}