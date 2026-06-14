# Waddler Tourism — Frontend

React + Vite frontend for the Waddler Tourism Hotel Booking System.

## Tech stack
- React 18
- React Router v6
- Axios (with JWT interceptor)
- Vite

## Setup

```bash
npm install
cp .env.example .env.local   # set VITE_API_URL if needed
npm run dev                  # runs on http://localhost:3000
```

Make sure the Spring Boot backend is running on port 8080.

---

## Folder structure

```
src/
├── api/                  # All API calls (one file per domain)
│   ├── axiosInstance.js  # Axios base + JWT interceptor  (shared)
│   ├── authApi.js        # login, register
│   ├── hotelApi.js       # hotel listing & detail        (Salma)
│   ├── reservationApi.js # book, cancel, my reservations (Salma)
│   ├── adminApi.js       # admin CRUD                    (Jiovanni)
│   └── managerApi.js     # rooms & events                (Zeina)
│
├── context/
│   └── AuthContext.jsx   # user, token, login, logout, hasRole (Zeina)
│
├── routes/
│   ├── AppRoutes.jsx     # all routes in one place       (shared)
│   └── ProtectedRoute.jsx# role-based route guard        (Zeina)
│
├── components/shared/
│   └── Navbar.jsx        # role-aware navbar             (Zeina)
│
├── pages/
│   ├── auth/             # LoginPage, RegisterPage        (Salma)
│   ├── user/             # Hotel list/detail, reservations, profile (Salma)
│   ├── manager/          # ManagerDashboard, Rooms, Events (Zeina)
│   ├── admin/            # AdminDashboard, Hotels, Users, Reservations (Jiovanni)
│   └── UnauthorizedPage.jsx
│
└── App.jsx
```

---

## Roles (from Spring Security)

| Role | Constant | Access |
|------|----------|--------|
| Admin | `ROLE_ADMIN` | Everything |
| Hotel Manager | `ROLE_HOTEL_MANAGER` | Manager panel + user pages |
| User | `ROLE_USER` | User pages only |

---

## Team assignments

| Member | Pages |
|--------|-------|
| **Salma** | LoginPage, RegisterPage, HotelListPage, HotelDetailPage, ReservationsPage, ProfilePage |
| **Jiovanni** | AdminDashboard, AdminHotelsPage, AdminUsersPage, AdminReservationsPage |
| **Zeina** | ManagerDashboard, ManageRoomsPage, ManageEventsPage, Navbar, AuthContext, ProtectedRoute, HomePage |

---

## Git workflow (suggested)

```
main          ← stable, always deployable
├── salma/auth
├── salma/user-pages
├── jiovanni/admin
└── zeina/manager-shared
```

Open a PR to main when your feature is ready.
