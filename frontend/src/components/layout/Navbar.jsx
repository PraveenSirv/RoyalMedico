import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Moon, Sun, HeartPulse } from 'lucide-react';

const Navbar = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const [isDarkMode, setIsDarkMode] = React.useState(true);

  const toggleDarkMode = () => {
    setIsDarkMode(!isDarkMode);
    document.documentElement.classList.toggle('dark');
  };

  return (
    <nav className="fixed top-0 left-0 right-0 z-50 glass backdrop-blur-md border-b border-white/10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center">
            <Link to="/" className="flex items-center gap-2 text-white font-bold text-xl">
              <HeartPulse className="h-6 w-6 text-cyan-400" />
              <span>Royal Medico</span>
            </Link>
          </div>
          
          <div className="hidden md:block">
            <div className="ml-10 flex items-baseline space-x-4">
              <Link to="/" className="text-slate-300 hover:text-white px-3 py-2 rounded-md text-sm font-medium transition-colors">Home</Link>
              <a href="#features" className="text-slate-300 hover:text-white px-3 py-2 rounded-md text-sm font-medium transition-colors">Features</a>
              <a href="#about" className="text-slate-300 hover:text-white px-3 py-2 rounded-md text-sm font-medium transition-colors">About</a>
              <a href="#contact" className="text-slate-300 hover:text-white px-3 py-2 rounded-md text-sm font-medium transition-colors">Contact</a>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <button 
              onClick={toggleDarkMode}
              className="p-2 rounded-full bg-white/10 hover:bg-white/20 transition-colors text-white"
            >
              {isDarkMode ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
            </button>

            {isAuthenticated ? (
              <div className="flex items-center gap-4">
                <span className="text-white text-sm">Hello, {user?.email}</span>
                <Link 
                  to={
                    user?.role === 'ADMIN' ? '/admin/dashboard' : 
                    user?.role === 'PHARMACIST' ? '/pharmacist/dashboard' : 
                    '/customer/dashboard'
                  }
                  className="bg-cyan-500 hover:bg-cyan-600 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
                >
                  Dashboard
                </Link>
                <button 
                  onClick={logout}
                  className="bg-white/10 hover:bg-white/20 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
                >
                  Logout
                </button>
              </div>
            ) : (
              <div className="flex items-center gap-4">
                <Link to="/login" className="text-white hover:text-cyan-400 px-3 py-2 rounded-md text-sm font-medium transition-colors">Login</Link>
                <Link 
                  to="/register" 
                  className="bg-cyan-500 hover:bg-cyan-600 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors shadow-lg shadow-cyan-500/30"
                >
                  Register
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
