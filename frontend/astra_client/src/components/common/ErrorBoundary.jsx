import React from 'react';

export default class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, message: '' };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, message: error?.message || 'Unexpected application error.' };
  }

  componentDidCatch(error, info) {
    console.error('ASTRA UI error', error, info);
  }

  render() {
    if (!this.state.hasError) return this.props.children;
    return (
      <main className="error-page" role="alert">
        <div className="empty-card">
          <p className="eyebrow">ASTRA</p>
          <h1>Something went wrong</h1>
          <p className="muted">{this.state.message}</p>
          <button className="primary-btn" onClick={() => window.location.reload()}>Reload</button>
        </div>
      </main>
    );
  }
}
