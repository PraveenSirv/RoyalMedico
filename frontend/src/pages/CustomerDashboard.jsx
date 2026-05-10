import React from 'react';
import { useAuth } from '../context/AuthContext';
import { motion } from 'framer-motion';
import { ShoppingBag, FileText, Heart, Activity, LogOut } from 'lucide-react';

const CustomerDashboard = () => {
  const { logout, user } = useAuth();

  return (
    <div className="min-h-screen bg-slate-900 flex text-white">
      {/* Sidebar */}
      <div className="w-64 bg-slate-950 border-r border-white/5 p-6 flex flex-col justify-between">
        <div>
          <div className="flex items-center gap-2 font-bold text-xl mb-10">
            <Activity className="text-cyan-400" />
            <span>Royal Medico</span>
          </div>
          
          <nav className="space-y-2">
            <a href="#" className="flex items-center gap-3 px-4 py-2.5 bg-cyan-500/10 text-cyan-400 rounded-lg text-sm font-medium">
              <ShoppingBag className="h-5 w-5" />
              Browse Medicines
            </a>
            <a href="#" className="flex items-center gap-3 px-4 py-2.5 text-slate-400 hover:text-white hover:bg-white/5 rounded-lg text-sm font-medium transition-colors">
              <FileText className="h-5 w-5" />
              My Prescriptions
            </a>
            <a href="#" className="flex items-center gap-3 px-4 py-2.5 text-slate-400 hover:text-white hover:bg-white/5 rounded-lg text-sm font-medium transition-colors">
              <Heart className="h-5 w-5" />
              Health Profile
            </a>
          </nav>
        </div>

        <button 
          onClick={logout}
          className="flex items-center gap-3 px-4 py-2.5 text-slate-400 hover:text-white hover:bg-white/5 rounded-lg text-sm font-medium transition-colors"
        >
          <LogOut className="h-5 w-5" />
          Logout
        </button>
      </div>

      {/* Main Content */}
      <div className="flex-grow p-8">
        <header className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-2xl font-bold text-white">Customer Dashboard</h1>
            <p className="text-slate-400 text-sm">Welcome back, {user?.email}</p>
          </div>
          <div className="h-10 w-10 bg-white/10 rounded-full flex items-center justify-center">
            CU
          </div>
        </header>

        {/* Welcome Section */}
        <div className="glass-morphic p-6 rounded-xl mb-8">
          <h2 className="text-xl font-bold text-white mb-2">Welcome to Royal Medico</h2>
          <p className="text-slate-300 text-sm">You can browse medicines, upload prescriptions, and manage your health profile here.</p>
        </div>

        {/* Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="glass p-6 rounded-xl border border-white/5">
            <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
              <ShoppingBag className="text-cyan-400" />
              Recent Orders
            </h3>
            <p className="text-slate-400 text-sm">You have no recent orders.</p>
          </div>
          <div className="glass p-6 rounded-xl border border-white/5">
            <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
              <FileText className="text-purple-400" />
              Recent Prescriptions
            </h3>
            <p className="text-slate-400 text-sm">You have no recent prescriptions.</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CustomerDashboard;
