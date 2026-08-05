import { lazy, Suspense, type ReactNode } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider, useAuth } from './auth/AuthProvider';
import { GuestRoute, ProtectedRoute } from './auth/ProtectedRoute';
import { RoleGuard } from './auth/RoleGuard';
import { Roles, homePathForRole } from './auth/roles';
import { RouteFallback } from './components/ui/RouteFallback';
import { CartProvider } from './features/cart/CartContext';
import { AdminShell } from './layouts/AdminShell';
import { CustomerShell } from './layouts/CustomerShell';

const LoginPage = lazy(() =>
  import('./pages/LoginPage').then((m) => ({ default: m.LoginPage })),
);
const RegisterPage = lazy(() =>
  import('./pages/RegisterPage').then((m) => ({ default: m.RegisterPage })),
);
const ProfilePage = lazy(() =>
  import('./pages/ProfilePage').then((m) => ({ default: m.ProfilePage })),
);
const EditProfilePage = lazy(() =>
  import('./pages/EditProfilePage').then((m) => ({ default: m.EditProfilePage })),
);

const CustomerDashboardPage = lazy(() =>
  import('./pages/customer/CustomerDashboardPage').then((m) => ({
    default: m.CustomerDashboardPage,
  })),
);
const BrowseBooksPage = lazy(() =>
  import('./pages/customer/BrowseBooksPage').then((m) => ({ default: m.BrowseBooksPage })),
);
const CustomerBookDetailsPage = lazy(() =>
  import('./pages/customer/CustomerBookDetailsPage').then((m) => ({
    default: m.CustomerBookDetailsPage,
  })),
);
const CartPage = lazy(() =>
  import('./pages/customer/CartPage').then((m) => ({ default: m.CartPage })),
);
const CheckoutPage = lazy(() =>
  import('./pages/customer/CheckoutPage').then((m) => ({ default: m.CheckoutPage })),
);
const OrdersPage = lazy(() =>
  import('./pages/customer/OrdersPage').then((m) => ({ default: m.OrdersPage })),
);
const OrderConfirmationPage = lazy(() =>
  import('./pages/customer/OrderConfirmationPage').then((m) => ({
    default: m.OrderConfirmationPage,
  })),
);
const PaymentPage = lazy(() =>
  import('./pages/customer/PaymentPage').then((m) => ({ default: m.PaymentPage })),
);
const PaymentSuccessPage = lazy(() =>
  import('./pages/customer/PaymentSuccessPage').then((m) => ({ default: m.PaymentSuccessPage })),
);
const PaymentFailurePage = lazy(() =>
  import('./pages/customer/PaymentFailurePage').then((m) => ({ default: m.PaymentFailurePage })),
);

const AdminDashboardPage = lazy(() =>
  import('./pages/admin/AdminDashboardPage').then((m) => ({ default: m.AdminDashboardPage })),
);
const AdminAnalyticsPage = lazy(() =>
  import('./pages/admin/AdminAnalyticsPage').then((m) => ({ default: m.AdminAnalyticsPage })),
);
const AdminManageBooksPage = lazy(() =>
  import('./pages/admin/AdminManageBooksPage').then((m) => ({ default: m.AdminManageBooksPage })),
);
const AdminCreateBookPage = lazy(() =>
  import('./pages/admin/AdminCreateBookPage').then((m) => ({ default: m.AdminCreateBookPage })),
);
const AdminEditBookPage = lazy(() =>
  import('./pages/admin/AdminEditBookPage').then((m) => ({ default: m.AdminEditBookPage })),
);
const AdminAuthorsPage = lazy(() =>
  import('./pages/admin/AdminAuthorsPage').then((m) => ({ default: m.AdminAuthorsPage })),
);
const AdminPublishersPage = lazy(() =>
  import('./pages/admin/AdminPublishersPage').then((m) => ({ default: m.AdminPublishersPage })),
);
const AdminCategoriesPage = lazy(() =>
  import('./pages/admin/AdminCategoriesPage').then((m) => ({ default: m.AdminCategoriesPage })),
);
const AdminUsersPage = lazy(() =>
  import('./pages/admin/AdminUsersPage').then((m) => ({ default: m.AdminUsersPage })),
);

function HomeRedirect() {
  const { user, isAuthenticated, isInitializing } = useAuth();
  if (isInitializing) return null;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <Navigate to={homePathForRole(user?.role)} replace />;
}

function LazyPage({ children }: { children: ReactNode }) {
  return <Suspense fallback={<RouteFallback />}>{children}</Suspense>;
}

export default function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <Routes>
          <Route element={<GuestRoute />}>
            <Route
              path="/login"
              element={
                <LazyPage>
                  <LoginPage />
                </LazyPage>
              }
            />
            <Route
              path="/register"
              element={
                <LazyPage>
                  <RegisterPage />
                </LazyPage>
              }
            />
          </Route>

          <Route element={<ProtectedRoute />}>
            <Route element={<RoleGuard allow={[Roles.USER]} />}>
              <Route element={<CustomerShell />}>
                <Route
                  path="/dashboard"
                  element={
                    <LazyPage>
                      <CustomerDashboardPage />
                    </LazyPage>
                  }
                />
                <Route
                  path="/books"
                  element={
                    <LazyPage>
                      <BrowseBooksPage />
                    </LazyPage>
                  }
                />
                <Route
                  path="/books/:id"
                  element={
                    <LazyPage>
                      <CustomerBookDetailsPage />
                    </LazyPage>
                  }
                />
                <Route
                  path="/cart"
                  element={
                    <LazyPage>
                      <CartPage />
                    </LazyPage>
                  }
                />
                <Route
                  path="/checkout"
                  element={
                    <LazyPage>
                      <CheckoutPage />
                    </LazyPage>
                  }
                />
                <Route
                  path="/orders"
                  element={
                    <LazyPage>
                      <OrdersPage />
                    </LazyPage>
                  }
                />
                <Route
                  path="/checkout/payment/:paymentId"
                  element={
                    <LazyPage>
                      <PaymentPage />
                    </LazyPage>
                  }
                />
                <Route
                  path="/payment-success"
                  element={
                    <LazyPage>
                      <PaymentSuccessPage />
                    </LazyPage>
                  }
                />
                <Route
                  path="/checkout/payment/:paymentId/failed"
                  element={
                    <LazyPage>
                      <PaymentFailurePage />
                    </LazyPage>
                  }
                />
                <Route
                  path="/orders/:orderId"
                  element={
                    <LazyPage>
                      <OrderConfirmationPage />
                    </LazyPage>
                  }
                />
                <Route
                  path="/profile"
                  element={
                    <LazyPage>
                      <ProfilePage />
                    </LazyPage>
                  }
                />
                <Route
                  path="/profile/edit"
                  element={
                    <LazyPage>
                      <EditProfilePage />
                    </LazyPage>
                  }
                />
              </Route>
            </Route>

            <Route element={<RoleGuard allow={[Roles.ADMIN]} />}>
              <Route path="/admin" element={<AdminShell />}>
                <Route
                  path="dashboard"
                  element={
                    <LazyPage>
                      <AdminDashboardPage />
                    </LazyPage>
                  }
                />
                <Route
                  path="analytics"
                  element={
                    <LazyPage>
                      <AdminAnalyticsPage />
                    </LazyPage>
                  }
                />
                <Route
                  path="books"
                  element={
                    <LazyPage>
                      <AdminManageBooksPage />
                    </LazyPage>
                  }
                />
                <Route
                  path="books/new"
                  element={
                    <LazyPage>
                      <AdminCreateBookPage />
                    </LazyPage>
                  }
                />
                <Route
                  path="books/:id/edit"
                  element={
                    <LazyPage>
                      <AdminEditBookPage />
                    </LazyPage>
                  }
                />
                <Route
                  path="authors"
                  element={
                    <LazyPage>
                      <AdminAuthorsPage />
                    </LazyPage>
                  }
                />
                <Route
                  path="publishers"
                  element={
                    <LazyPage>
                      <AdminPublishersPage />
                    </LazyPage>
                  }
                />
                <Route
                  path="categories"
                  element={
                    <LazyPage>
                      <AdminCategoriesPage />
                    </LazyPage>
                  }
                />
                <Route
                  path="users"
                  element={
                    <LazyPage>
                      <AdminUsersPage />
                    </LazyPage>
                  }
                />
              </Route>
            </Route>
          </Route>

          <Route path="/" element={<HomeRedirect />} />
          <Route path="*" element={<HomeRedirect />} />
        </Routes>
      </CartProvider>
    </AuthProvider>
  );
}
