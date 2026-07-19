import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";

const linkClass = ({ isActive }: { isActive: boolean }) =>
  `px-3 py-1.5 rounded-md text-sm font-medium ${
    isActive ? "bg-primary-600 text-white" : "text-gray-600 hover:bg-gray-100"
  }`;

export default function AppLayout() {
  const { isAuthenticated, isAdmin, user, logout } = useAuth();

  return (
    <div className="flex h-screen flex-col">
      <nav className="flex shrink-0 items-center justify-between border-b bg-white px-5 py-2.5 shadow-sm">
        <div className="flex items-center gap-1 overflow-x-auto">
          <span className="mr-3 text-sm font-semibold text-gray-800">🏙️ SmartCity</span>
          <NavLink to="/route" className={linkClass}>Route</NavLink>
          <NavLink to="/map" className={linkClass}>Map</NavLink>
          <NavLink to="/nearby" className={linkClass}>Nearby</NavLink>
          <NavLink to="/traffic" className={linkClass}>Traffic</NavLink>
          <NavLink to="/analytics" className={linkClass}>Analytics</NavLink>
          {isAuthenticated && <NavLink to="/history" className={linkClass}>History</NavLink>}
          {isAdmin && <NavLink to="/admin" className={linkClass}>Admin</NavLink>}
        </div>

        <div className="flex shrink-0 items-center gap-3">
          {isAuthenticated ? (
            <>
              <span className="text-sm text-gray-500">{user?.email}</span>
              <button
                onClick={logout}
                className="rounded-md border border-gray-300 px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-50"
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" className={linkClass}>Login</NavLink>
              <NavLink to="/register" className={linkClass}>Register</NavLink>
            </>
          )}
        </div>
      </nav>

      <div className="flex-1 overflow-hidden">
        <Outlet />
      </div>
    </div>
  );
}
