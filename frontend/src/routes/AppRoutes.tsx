import { Routes, Route, Navigate } from "react-router-dom";
import { ROLES } from "../context/AuthContext";
import ProtectedRoute from "./ProtectedRoute";

import Welcome from "../pages/Welcome";
import Home from "../pages/Home";
import GuestHotelsPage from "../pages/guest/HotelsPage";
import GuestHotelDetailPage from "../pages/guest/HotelDetailPage";
import GuestAvailabilityPage from "../pages/guest/AvailabilityPage";
import GuestContactPage from "../pages/guest/ContactPage";
import Login from "../pages/auth/Login";
import Register from "../pages/auth/Register";
import NotFound from "../pages/NotFound";
// ── Salma: Penguin UI pages ───────────────────────────────────────────────────

import AboutPage from "../pages/AboutPage";
import UnauthorizedPage from "../pages/UnauthorizedPage";
import UserHomePage from "../pages/user/HomePage";
import UserBookingPage from "../pages/user/UserBookingPage";
import UserBookingsPage from "../pages/user/UserBookingsPage";
import ReservationsPage from "../pages/user/ReservationsPage";
import PaymentPage from "../pages/user/PaymentPage";
import UserHotelsPage from "../pages/user/UserHotelsPage";
import UserHotelDetailPage from "../pages/user/UserHotelDetailPage";
import UserAvailabilityPage from "../pages/user/UserAvailabilityPage";
import UserContactPage from "../pages/user/UserContactPage";
import UserReviewPage from "../pages/user/UserReviewPage";
import UserProfilePage from "../pages/user/UserProfilePage";
import UserLoyaltyPage from "../pages/user/UserLoyaltyPage";
import UserNotificationsPage from "../pages/user/UserNotificationsPage";

import ManagerDashboard from "../pages/manager/ManagerDashboard";
import ManageRoomsPage from "../pages/manager/ManageRoomsPage";
import ManageEventsPage from "../pages/manager/ManageEventsPage";
import ManagerBookingsPage from "../pages/manager/ManagerBookingsPage";
import ManagerProfilePage from "../pages/manager/ManagerProfilePage";
import ManagerShell from "../components/ManagerShell";

import AdminDashboard from "../pages/admin/AdminDashboard";
import AdminHotelsPage from "../pages/admin/AdminHotelsPage";
import AdminLicensesPage from "../pages/admin/AdminLicensesPage";
import AdminUsersPage from "../pages/admin/AdminUsersPage";
import AdminBookingsPage from "../pages/admin/AdminBookingsPage";
import AdminPaymentsPage from "../pages/admin/AdminPaymentsPage";
import AdminContactsPage from "../pages/admin/AdminContactsPage";
import AdminPoliciesPage from "../pages/admin/AdminPoliciesPage";
import AdminProfilePage from "../pages/admin/AdminProfilePage";
import AdminNotificationsPage from "../pages/admin/AdminNotificationsPage";

const UserOnly = ({ children }: { children: React.ReactNode }) => (
  <ProtectedRoute roles={[ROLES.USER]}>{children}</ProtectedRoute>
);

const ManagerOnly = ({ children }: { children: React.ReactNode }) => (
  <ProtectedRoute roles={[ROLES.MANAGER, ROLES.ADMIN]}>{children}</ProtectedRoute>
);

const AdminOnly = ({ children }: { children: React.ReactNode }) => (
  <ProtectedRoute roles={[ROLES.ADMIN]}>{children}</ProtectedRoute>
);

const AppRoutes = () => (
  <Routes>
    <Route path="/" element={<Welcome />} />
    <Route path="/home" element={<Home />} />
    <Route path="/login" element={<Login />} />
    <Route path="/register" element={<Register />} />
    {/* ── Public ──────────────────────────────────────────────────────── */}
    <Route path="/unauthorized" element={<UnauthorizedPage />} />
    <Route path="/about" element={<AboutPage />} />
    <Route path="/splash" element={<Navigate to="/" replace />} />

    <Route path="/guest" element={<Navigate to="/guest/hotels" replace />} />
    <Route path="/guest/hotels" element={<GuestHotelsPage />} />
    <Route path="/guest/hotel/:id" element={<GuestHotelDetailPage />} />
    <Route path="/guest/availability" element={<GuestAvailabilityPage />} />
    <Route path="/guest/contact" element={<GuestContactPage />} />

    <Route path="/user" element={<UserOnly><Navigate to="/user/bookings" replace /></UserOnly>} />
    <Route path="/user/home" element={<UserOnly><UserHomePage /></UserOnly>} />
    <Route path="/user/hotels" element={<UserOnly><UserHotelsPage /></UserOnly>} />
    <Route path="/user/hotel/:id" element={<UserOnly><UserHotelDetailPage /></UserOnly>} />
    <Route path="/user/availability" element={<UserOnly><UserAvailabilityPage /></UserOnly>} />
    <Route path="/user/book/:id" element={<UserOnly><UserBookingPage /></UserOnly>} />
    <Route path="/user/bookings" element={<UserOnly><UserBookingsPage /></UserOnly>} />
    <Route path="/user/reservations" element={<UserOnly><ReservationsPage /></UserOnly>} />
    <Route path="/user/review/:id" element={<UserOnly><UserReviewPage /></UserOnly>} />
    <Route path="/user/profile" element={<UserOnly><UserProfilePage /></UserOnly>} />
    <Route path="/user/loyalty" element={<UserOnly><UserLoyaltyPage /></UserOnly>} />
    <Route path="/user/notifications" element={<UserOnly><UserNotificationsPage /></UserOnly>} />
    <Route path="/user/contact" element={<UserOnly><UserContactPage /></UserOnly>} />
    <Route path="/user/support" element={<UserOnly><UserContactPage /></UserOnly>} />
    <Route path="/user/payments/:bookingId" element={<UserOnly><PaymentPage /></UserOnly>} />
    <Route path="/user/bookings/:bookingId/pay" element={<UserOnly><PaymentPage /></UserOnly>} />

    <Route path="/manager" element={<ManagerOnly><ManagerShell><ManagerDashboard /></ManagerShell></ManagerOnly>} />
    <Route path="/manager/rooms" element={<ManagerOnly><ManagerShell><ManageRoomsPage /></ManagerShell></ManagerOnly>} />
    <Route path="/manager/bookings" element={<ManagerOnly><ManagerShell><ManagerBookingsPage /></ManagerShell></ManagerOnly>} />
    <Route path="/manager/events" element={<ManagerOnly><ManagerShell><ManageEventsPage /></ManagerShell></ManagerOnly>} />
    <Route path="/manager/profile" element={
      <ProtectedRoute roles={[ROLES.MANAGER]}>
        <ManagerShell><ManagerProfilePage /></ManagerShell>
      </ProtectedRoute>
    } />

    <Route path="/admin" element={<AdminOnly><AdminDashboard /></AdminOnly>} />
    <Route path="/admin/hotels" element={<AdminOnly><AdminHotelsPage /></AdminOnly>} />
    <Route path="/admin/licenses" element={<AdminOnly><AdminLicensesPage /></AdminOnly>} />
    <Route path="/admin/users" element={<AdminOnly><AdminUsersPage /></AdminOnly>} />
    <Route path="/admin/bookings" element={<AdminOnly><AdminBookingsPage /></AdminOnly>} />
    <Route path="/admin/payments" element={<AdminOnly><AdminPaymentsPage /></AdminOnly>} />
    <Route path="/admin/contacts" element={<AdminOnly><AdminContactsPage /></AdminOnly>} />
    <Route path="/admin/policies" element={<AdminOnly><AdminPoliciesPage /></AdminOnly>} />
    <Route path="/admin/profile" element={<AdminOnly><AdminProfilePage /></AdminOnly>} />
    <Route path="/admin/notifications" element={<AdminOnly><AdminNotificationsPage /></AdminOnly>} />

    <Route path="/hotels" element={<Navigate to="/guest/hotels" replace />} />
    <Route path="/hotel/:id" element={<Navigate to="/guest/hotel/:id" replace />} />
    <Route path="/availability" element={<Navigate to="/guest/availability" replace />} />
    <Route path="/contact" element={<Navigate to="/guest/contact" replace />} />
    <Route path="/book/:id" element={<Navigate to="/user/book/:id" replace />} />
    <Route path="/bookings" element={<Navigate to="/user/bookings" replace />} />
    <Route path="/my-reservations" element={<Navigate to="/user/reservations" replace />} />
    <Route path="/review/:id" element={<Navigate to="/user/review/:id" replace />} />
    <Route path="/profile" element={<Navigate to="/user/profile" replace />} />
    <Route path="/loyalty" element={<Navigate to="/user/loyalty" replace />} />
    <Route path="/notifications" element={<Navigate to="/user/notifications" replace />} />
    <Route path="/payments/:bookingId" element={<Navigate to="/user/payments/:bookingId" replace />} />
    <Route path="/bookings/:bookingId/pay" element={<Navigate to="/user/bookings/:bookingId/pay" replace />} />

    <Route path="*" element={<NotFound />} />
  </Routes>
);

export default AppRoutes;
