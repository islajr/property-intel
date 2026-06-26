import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { Menu, X, Bell, LogOut } from 'lucide-react';

export default function Navigation() {
  const { isAuthenticated, clearToken, accessToken } = useAuthStore();
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);

  // Helper to extract email/avatar info (in memory only)
  // For now, since we store only the token in-memory, we can show a placeholder "U" or parse the token if JWT is present
  const getUserInitial = () => {
    return 'U';
  };

  const handleSignOut = () => {
    // Call clearToken and navigate
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

      {/* Mobile Drawer (Basic layout, will be polished in Wave D) */}
      {mobileMenuOpen && (
        <div className="mobile-drawer">
          <div className="mobile-drawer-links">
            <Link to="/market" className="mobile-drawer-item" onClick={() => setMobileMenuOpen(false)}>
              Market
            </Link>
            <Link to="/listings" className="mobile-drawer-item" onClick={() => setMobileMenuOpen(false)}>
              Listings
            </Link>
            {isAuthenticated && (
              <Link to="/alerts" className="mobile-drawer-item" onClick={() => setMobileMenuOpen(false)}>
                My Alerts
              </Link>
            )}
            
            <div className="mobile-drawer-divider" />
            
            {isAuthenticated ? (
              <button onClick={() => { handleSignOut(); setMobileMenuOpen(false); }} className="mobile-drawer-btn-destructive">
                Sign out
              </button>
            ) : (
              <div className="mobile-drawer-auth-actions">
                <Link to="/login" className="mobile-drawer-login" onClick={() => setMobileMenuOpen(false)}>
                  Login
                </Link>
                <Link to="/register" className="mobile-drawer-register" onClick={() => setMobileMenuOpen(false)}>
                  Register
                </Link>
              </div>
            )}
          </div>
        </div>
      )}
    </nav>
  );
}
