import React from 'react';
import { useAuth } from '../context/AuthContext';
import { motion } from 'framer-motion';
import { Pill, ClipboardList, Package, Activity, LogOut } from 'lucide-react';

const PharmacistDashboard = () => {
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
              <Pill className="h-5 w-5" />
              Inventory
            </a>
            <a href="#" className="flex items-center gap-3 px-4 py-2.5 text-slate-400 hover:text-white hover:bg-white/5 rounded-lg text-sm font-medium transition-colors">
              <ClipboardList className="h-5 w-5" />
              Prescriptions
            </a>
            <a href="#" className="flex items-center gap-3 px-4 py-2.5 text-slate-400 hover:text-white hover:bg-white/5 rounded-lg text-sm font-medium transition-colors">
              <Package className="h-5 w-5" />
              Orders
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
            <h1 className="text-2xl font-bold text-white">Pharmacist Dashboard</h1>
            <p className="text-slate-400 text-sm">Welcome back, {user?.email}</p>
          </div>
          <div className="h-10 w-10 bg-white/10 rounded-full flex items-center justify-center">
            PH
          </div>
        </header>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div className="glass p-6 rounded-xl border border-white/5">
            <div className="text-slate-400 text-sm mb-1">Total Medicines</div>
            <div className="text-2xl font-bold text-white">450</div>
            <div className="text-cyan-400 text-xs mt-1">20 low in stock</div>
          </div>
          <div className="glass p-6 rounded-xl border border-white/5">
            <div className="text-slate-400 text-sm mb-1">Pending Orders</div>
            <div className="text-2xl font-bold text-white">15</div>
            <div className="text-yellow-400 text-xs mt-1">Requires attention</div>
          </div>
          <div className="glass p-6 rounded-xl border border-white/5">
            <div className="text-slate-400 text-sm mb-1">Processed Today</div>
            <div className="text-2xl font-bold text-white">34</div>
            <div className="text-green-400 text-xs mt-1">+5 from yesterday</div>
          </div>
        </div>

        {/* Recent Activity */}
        <div className="glass p-6 rounded-xl border border-white/5">
          <h2 className="text-lg font-semibold mb-4">Pending Prescriptions</h2>
          <div className="space-y-4">
            <div className="flex items-center justify-between text-sm border-b border-white/5 pb-2">
              <div>
                <span className="font-medium text-white">Paracetamol 500mg</span>
                <p className="text-slate-400 text-xs">For Patient #102</p>
              </div>
              <span className="text-yellow-400">Pending</span>
            </div>
            <div className="flex items-center justify-between text-sm border-b border-white/5 pb-2">
              <div>
                <span className="font-medium text-white">Amoxicillin 250mg</span>
                <p className="text-slate-400 text-xs">For Patient #105</p>
              </div>
              <span className="text-yellow-400">Pending</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PharmacistDashboard;
