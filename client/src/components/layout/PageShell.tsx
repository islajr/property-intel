import { Outlet, useLocation } from 'react-router-dom';
import Navigation from './Navigation';
import Footer from './Footer';

export default function PageShell() {
  const location = useLocation();

  return (
    <div className="app-shell">
      <Navigation />
      <main className="main-content">
        <div key={location.pathname} className="page-transition-wrapper">
          <Outlet />
        </div>
      </main>
      <Footer />
    </div>
  );
}
