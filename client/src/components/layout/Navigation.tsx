import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { Menu, X, Bell, LogOut } from 'lucide-react';
import { logout } from '../../api/auth';

export default function Navigation() {
  const { isAuthenticated, clearToken } = useAuthStore();
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);

  const getUserInitial = () => {
    const token = useAuthStore.getState().accessToken;
    if (!token) return 'U';
    try {
      const parts = token.split('.');
      if (parts.length === 3 && parts[1]) {
        const base64Url = parts[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
          window.atob(base64)
            .split('')
            .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
            .join('')
        );
        const payload = JSON.parse(jsonPayload);
        const email = payload.sub || payload.email || 'U';
        return email.charAt(0).toUpperCase();
      }
    } catch (e) {
      // fallback
    }
    return 'U';
  };

  const handleSignOut = async () => {
    try {
      await logout();
    } catch (error) {
      // Allow local logout if API fails
    }
    clearToken();
    setDropdownOpen(false);
    navigate('/');
  };

  return (
    <nav className="nav-bar">
      <div className="nav-container container">
        {/* Logo Wordmark */}
        <Link to="/" className="nav-logo" onClick={() => setMobileMenuOpen(false)}>
          <span className="logo-pi font-numeric">PI</span>
          <span className="logo-text">Property Intel</span>
        </Link>

        {/* Desktop Links */}
        <div className="nav-links desktop-only">
          <NavLink to="/market" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}>
            Market
          </NavLink>
          <NavLink to="/listings" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}>
            Listings
          </NavLink>
        </div>

        {/* Auth Area Desktop */}
        <div className="nav-auth desktop-only">
          {isAuthenticated ? (
            <div className="avatar-dropdown-container">
              <button 
                onClick={() => setDropdownOpen(!dropdownOpen)} 
                className="avatar-btn"
                aria-label="User menu"
              >
                {getUserInitial()}
              </button>
              {dropdownOpen && (
                <div className="avatar-dropdown">
                  <Link to="/alerts" className="dropdown-item" onClick={() => setDropdownOpen(false)}>
                    <Bell size={16} className="dropdown-icon" />
                    My Alerts
                  </Link>
                  <button onClick={handleSignOut} className="dropdown-item destructive">
                    <LogOut size={16} className="dropdown-icon" />
                    Sign out
                  </button>
                </div>
              )}
            </div>
          ) : (
            <div className="nav-actions">
              <Link to="/login" className="nav-login-link">
                Login
              </Link>
              <Link to="/register" className="nav-register-btn">
                Register
              </Link>
            </div>
          )}
        </div>

        {/* Mobile Hamburger Toggle */}
        <button 
          className="mobile-toggle-btn mobile-only"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          aria-label={mobileMenuOpen ? 'Close menu' : 'Open menu'}
        >
          {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
        </button>
      </div>

      {/* Mobile Drawer */}
      {mobileMenuOpen && (
        <div className="mobile-drawer">
          <div className="mobile-drawer-content flex flex-col justify-between h-full w-full">
            <div className="mobile-drawer-links flex flex-col gap-1">
              <Link 
                to="/market" 
                className="mobile-drawer-item flex items-center" 
                style={{ minHeight: 48 }}
                onClick={() => setMobileMenuOpen(false)}
              >
                Market
              </Link>
              <Link 
                to="/listings" 
                className="mobile-drawer-item flex items-center" 
                style={{ minHeight: 48 }}
                onClick={() => setMobileMenuOpen(false)}
              >
                Listings
              </Link>
              {isAuthenticated && (
                <Link 
                  to="/alerts" 
                  className="mobile-drawer-item flex items-center" 
                  style={{ minHeight: 48 }}
                  onClick={() => setMobileMenuOpen(false)}
                >
                  My Alerts
                </Link>
              )}
            </div>
            
            <div className="mobile-drawer-actions mt-auto w-full">
              <div className="mobile-drawer-divider" />
              {isAuthenticated ? (
                <button 
                  onClick={() => { handleSignOut(); setMobileMenuOpen(false); }} 
                  className="mobile-drawer-btn-destructive w-full flex items-center justify-center"
                  style={{ minHeight: 48 }}
                >
                  Sign out
                </button>
              ) : (
                <div className="mobile-drawer-auth-actions flex flex-col gap-3 w-full">
                  <Link 
                    to="/login" 
                    className="mobile-drawer-login w-full flex items-center justify-center" 
                    style={{ minHeight: 48 }}
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    Login
                  </Link>
                  <Link 
                    to="/register" 
                    className="mobile-drawer-register w-full flex items-center justify-center text-inverse font-semibold" 
                    style={{ minHeight: 48 }}
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    Register
                  </Link>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </nav>
  );
}
