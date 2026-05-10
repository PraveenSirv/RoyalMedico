import React from 'react';
import { Link } from 'react-router-dom';

const Unauthorized = () => {
  return (
    <div className="min-h-screen bg-slate-900 flex flex-col items-center justify-center text-white p-4">
      <h1 className="text-4xl font-bold mb-2 text-red-400">403 - Unauthorized</h1>
      <p className="text-slate-400 mb-6">You do not have permission to access this page.</p>
      <Link to="/" className="bg-cyan-500 hover:bg-cyan-600 text-white px-6 py-2.5 rounded-lg font-medium transition-colors">
        Go Home
      </Link>
    </div>
  );
};

export default Unauthorized;
