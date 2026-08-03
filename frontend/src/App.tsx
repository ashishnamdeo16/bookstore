import { Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider, useAuth } from './auth/AuthProvider';
import { GuestRoute, ProtectedRoute } from './auth/ProtectedRoute';
import { RoleGuard } from './auth/RoleGuard';
import { Roles, homePathForRole } from './auth/roles';
import { AdminShell } from './layouts/AdminShell';
import { CustomerShell } from './layouts/CustomerShell';
import { AdminAuthorsPage } from './pages/admin/AdminAuthorsPage';
import { AdminCategoriesPage } from './pages/admin/AdminCategoriesPage';
import { AdminCreateBookPage } from './pages/admin/AdminCreateBookPage';
import { AdminDashboardPage } from './pages/admin/AdminDashboardPage';
import { AdminEditBookPage } from './pages/admin/AdminEditBookPage';
import { AdminManageBooksPage } from './pages/admin/AdminManageBooksPage';
import { AdminPublishersPage } from './pages/admin/AdminPublishersPage';
import { AdminUsersPage } from './pages/admin/AdminUsersPage';
import { BrowseBooksPage } from './pages/customer/BrowseBooksPage';
import { CustomerBookDetailsPage } from './pages/customer/CustomerBookDetailsPage';
import { CustomerDashboardPage } from './pages/customer/CustomerDashboardPage';
import { CartPlaceholderPage, OrdersPlaceholderPage } from './pages/customer/Placeholders';
import { EditProfilePage } from './pages/EditProfilePage';
import { LoginPage } from './pages/LoginPage';
import { ProfilePage } from './pages/ProfilePage';
import { RegisterPage } from './pages/RegisterPage';

function HomeRedirect() {
  const { user, isAuthenticated, isInitializing } = useAuth();
  if (isInitializing) return null;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <Navigate to={homePathForRole(user?.role)} replace />;
}

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route element={<GuestRoute />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
        </Route>

        <Route element={<ProtectedRoute />}>
          <Route element={<RoleGuard allow={[Roles.USER]} />}>
            <Route element={<CustomerShell />}>
              <Route path="/dashboard" element={<CustomerDashboardPage />} />
              <Route path="/books" element={<BrowseBooksPage />} />
              <Route path="/books/:id" element={<CustomerBookDetailsPage />} />
              <Route path="/cart" element={<CartPlaceholderPage />} />
              <Route path="/orders" element={<OrdersPlaceholderPage />} />
              <Route path="/profile" element={<ProfilePage />} />
              <Route path="/profile/edit" element={<EditProfilePage />} />
            </Route>
          </Route>

          <Route element={<RoleGuard allow={[Roles.ADMIN]} />}>
            <Route path="/admin" element={<AdminShell />}>
              <Route path="dashboard" element={<AdminDashboardPage />} />
              <Route path="books" element={<AdminManageBooksPage />} />
              <Route path="books/new" element={<AdminCreateBookPage />} />
              <Route path="books/:id/edit" element={<AdminEditBookPage />} />
              <Route path="authors" element={<AdminAuthorsPage />} />
              <Route path="publishers" element={<AdminPublishersPage />} />
              <Route path="categories" element={<AdminCategoriesPage />} />
              <Route path="users" element={<AdminUsersPage />} />
            </Route>
          </Route>
        </Route>

        <Route path="/" element={<HomeRedirect />} />
        <Route path="*" element={<HomeRedirect />} />
      </Routes>
    </AuthProvider>
  );
}
