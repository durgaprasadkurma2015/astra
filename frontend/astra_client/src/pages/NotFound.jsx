import { Link } from 'react-router-dom';

export default function NotFound() {
  return <main className="error-page"><div className="empty-card"><p className="eyebrow">404</p><h1>Page not found</h1><p className="muted">The ASTRA page you requested does not exist.</p><Link className="primary-btn" to="/">Back to home</Link></div></main>;
}
