import React from 'react';
import { useAuth } from '../context/AuthContext';
import { motion } from 'framer-motion';
import { BarChart2, Users, ShoppingBag, Activity, LogOut } from 'lucide-react';

const AdminDashboard = () => {
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
              <BarChart2 className="h-5 w-5" />
              Dashboard
            </a>
            <a href="#" className="flex items-center gap-3 px-4 py-2.5 text-slate-400 hover:text-white hover:bg-white/5 rounded-lg text-sm font-medium transition-colors">
              <Users className="h-5 w-5" />
              Users
            </a>
            <a href="#" className="flex items-center gap-3 px-4 py-2.5 text-slate-400 hover:text-white hover:bg-white/5 rounded-lg text-sm font-medium transition-colors">
              <ShoppingBag className="h-5 w-5" />
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
            <h1 className="text-2xl font-bold text-white">Admin Dashboard</h1>
            <p className="text-slate-400 text-sm">Welcome back, {user?.email}</p>
          </div>
          <div className="h-10 w-10 bg-white/10 rounded-full flex items-center justify-center">
            AD
          </div>
        </header>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="glass p-6 rounded-xl border border-white/5">
            <div className="text-slate-400 text-sm mb-1">Total Users</div>
            <div className="text-2xl font-bold text-white">1,245</div>
            <div className="text-cyan-400 text-xs mt-1">+12% from last month</div>
          </div>
          <div className="glass p-6 rounded-xl border border-white/5">
            <div className="text-slate-400 text-sm mb-1">Total Orders</div>
            <div className="text-2xl font-bold text-white">856</div>
            <div className="text-cyan-400 text-xs mt-1">+5% from last month</div>
          </div>
          <div className="glass p-6 rounded-xl border border-white/5">
            <div className="text-slate-400 text-sm mb-1">Revenue</div>
            <div className="text-2xl font-bold text-white">$45,231</div>
            <div className="text-cyan-400 text-xs mt-1">+18% from last month</div>
          </div>
          <div className="glass p-6 rounded-xl border border-white/5">
            <div className="text-slate-400 text-sm mb-1">Active Prescriptions</div>
            <div className="text-2xl font-bold text-white">321</div>
            <div className="text-pink-400 text-xs mt-1">-2% from last month</div>
          </div>
        </div>

        {/* Recent Activity */}
        <div className="glass p-6 rounded-xl border border-white/5">
          <h2 className="text-lg font-semibold mb-4">Recent Activity</h2>
          <div className="space-y-4">
            <div className="flex items-center justify-between text-sm border-b border-white/5 pb-2">
              <div>
                <span className="font-medium text-white">New user registered</span>
                <p className="text-slate-400 text-xs">john@example.com</p>
              </div>
              <span className="text-slate-500">2 mins ago</span>
            </div>
            <div className="flex items-center justify-between text-sm border-b border-white/5 pb-2">
              <div>
                <span className="font-medium text-white">New order placed</span>
                <p className="text-slate-400 text-xs">Order #1234</p>
              </div>
              <span className="text-slate-500">10 mins ago</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
