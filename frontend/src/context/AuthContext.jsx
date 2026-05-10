import React, { createContext, useContext, useState, useEffect } from 'react';
import { jwtDecode } from 'jwt-decode';
import api from '../api/axios';

const AuthContext = createContext(undefined);

export const AuthProvider = ({ children }) => {
  const [state, setState] = useState({
    accessToken: localStorage.getItem('accessToken'),
    refreshToken: localStorage.getItem('refreshToken'),
    user: null,
    isLoading: true,
    isAuthenticated: false,
  });

  useEffect(() => {
    const initAuth = () => {
      const token = state.accessToken;
      if (token) {
        try {
          const decoded = jwtDecode(token);
          // Check expiration
          const currentTime = Date.now() / 1000;
          if (decoded.exp < currentTime) {
            // Token expired, let axios interceptor handle it
            setState((prev) => ({ ...prev, user: { role: decoded.role, email: decoded.sub }, isAuthenticated: true, isLoading: false }));
          } else {
            setState((prev) => ({ ...prev, user: { role: decoded.role, email: decoded.sub }, isAuthenticated: true, isLoading: false }));
          }
        } catch (error) {
          console.error('Failed to decode token', error);
          setState((prev) => ({ ...prev, isLoading: false }));
        }
      } else {
        setState((prev) => ({ ...prev, isLoading: false }));
      }
    };

    initAuth();
  }, [state.accessToken]);

  const login = async (email, password) => {
    setState((prev) => ({ ...prev, isLoading: true }));
    try {
      const response = await api.post('/api/auth/login', { email, password });
      const { accessToken, refreshToken } = response.data;
      
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      
      const decoded = jwtDecode(accessToken);
      
      setState({
        accessToken,
        refreshToken,
        user: { role: decoded.role, email: decoded.sub },
        isAuthenticated: true,
        isLoading: false,
      });
    } catch (error) {
      setState((prev) => ({ ...prev, isLoading: false }));
      throw error;
    }
  };

  const register = async (email, password, name) => {
    setState((prev) => ({ ...prev, isLoading: true }));
    try {
      // Role is hardcoded to CUSTOMER as per requirements
      const response = await api.post('/api/auth/register', { 
        email, 
        password, 
        name, 
        role: 'CUSTOMER' 
      });
      
      const { accessToken, refreshToken } = response.data;
      
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      
      const decoded = jwtDecode(accessToken);
      
      setState({
        accessToken,
        refreshToken,
        user: { role: decoded.role, email: decoded.sub },
        isAuthenticated: true,
        isLoading: false,
      });
    } catch (error) {
      setState((prev) => ({ ...prev, isLoading: false }));
      throw error;
    }
  };

  const logout = async () => {
    const token = localStorage.getItem('refreshToken');
    try {
      if (token) {
        await api.post('/api/auth/logout', { refreshToken: token });
      }
    } catch (error) {
      console.error('Logout failed on backend', error);
    } finally {
      localStorage.clear();
      setState({
        accessToken: null,
        refreshToken: null,
        user: null,
        isLoading: false,
        isAuthenticated: false,
      });
      window.location.href = '/';
    }
  };

  return (
    <AuthContext.Provider value={{ ...state, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
