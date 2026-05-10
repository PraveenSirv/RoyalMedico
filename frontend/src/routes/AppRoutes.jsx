import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Landing from '../pages/Landing';
import Login from '../pages/Login';
import Register from '../pages/Register';
import AdminDashboard from '../pages/AdminDashboard';
import PharmacistDashboard from '../pages/PharmacistDashboard';
import CustomerDashboard from '../pages/CustomerDashboard';
import Unauthorized from '../pages/Unauthorized';
import NotFound from '../pages/NotFound';
import ProtectedRoute from './ProtectedRoute';
import RoleRoute from './RoleRoute';

const AppRoutes = () => {
  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/" element={<Landing />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/unauthorized" element={<Unauthorized />} />

      {/* Protected Routes */}
      <Route element={<ProtectedRoute />}>
        {/* Role Based Routes */}
        <Route element={<RoleRoute allowedRoles={['ADMIN']} />}>
          <Route path="/admin/dashboard" element={<AdminDashboard />} />
        </Route>
        
        <Route element={<RoleRoute allowedRoles={['PHARMACIST']} />}>
          <Route path="/pharmacist/dashboard" element={<PharmacistDashboard />} />
        </Route>
        
        <Route element={<RoleRoute allowedRoles={['CUSTOMER']} />}>
          <Route path="/customer/dashboard" element={<CustomerDashboard />} />
        </Route>
      </Route>

      {/* Catch All */}
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
};

export default AppRoutes;
