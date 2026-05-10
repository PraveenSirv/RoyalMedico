import React, { Component } from 'react';

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    console.error('Uncaught error:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-slate-900 flex flex-col items-center justify-center text-white p-4">
          <h1 className="text-2xl font-bold mb-2">Something went wrong.</h1>
          <p className="text-slate-400 mb-4">We are working on fixing it.</p>
          <button 
            onClick={() => window.location.reload()}
            className="bg-cyan-500 hover:bg-cyan-600 text-white px-4 py-2 rounded-lg"
          >
            Reload Page
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
