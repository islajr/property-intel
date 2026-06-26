import { ExternalLink } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="footer-bar">
      <div className="footer-container container">
        <div className="footer-info">
          <span>Property Intelligence Platform</span>
          <span className="footer-divider-dot">·</span>
          <span className="font-numeric">© 2026</span>
        </div>
        <div className="footer-links">
          <a 
            href="https://api.property-intel.railway.app/swagger-ui.html" 
            target="_blank" 
            rel="noopener noreferrer"
            className="footer-link"
          >
            API Docs
            <ExternalLink size={12} className="footer-link-icon" />
          </a>
          <a 
            href="https://github.com" 
            target="_blank" 
            rel="noopener noreferrer"
            className="footer-link"
          >
            GitHub
            <ExternalLink size={12} className="footer-link-icon" />
          </a>
          <span className="footer-version">Stage 1 · v0.1.0</span>
        </div>
      </div>
    </footer>
  );
}
