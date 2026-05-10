import React from 'react';
import { motion } from 'framer-motion';
import { ArrowRight, Activity, Shield, Zap, Users } from 'lucide-react';
import { Link } from 'react-router-dom';
import Navbar from '../components/layout/Navbar';
import Footer from '../components/layout/Footer';

const Landing = () => {
  return (
    <div className="bg-slate-900 min-h-screen text-white overflow-hidden flex flex-col">
      <Navbar />
      
      {/* Hero Section */}
      <section className="relative pt-32 pb-20 md:pt-40 md:pb-32 flex-grow">
        {/* Background Gradients */}
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-full max-w-7xl h-[500px] opacity-30 blur-3xl pointer-events-none">
          <div className="absolute top-0 left-1/4 w-96 h-96 bg-cyan-500 rounded-full"></div>
          <div className="absolute top-0 right-1/4 w-96 h-96 bg-purple-500 rounded-full"></div>
        </div>

        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
            <motion.div 
              initial={{ opacity: 0, x: -50 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.8 }}
              className="space-y-8"
            >
              <div className="inline-flex items-center px-4 py-1.5 rounded-full border border-cyan-500/30 bg-cyan-500/10 text-cyan-400 text-sm font-medium">
                <span className="flex h-2 w-2 mr-2">
                  <span className="animate-ping absolute inline-flex h-2 w-2 rounded-full bg-cyan-400 opacity-75"></span>
                  <span className="relative inline-flex rounded-full h-2 w-2 bg-cyan-500"></span>
                </span>
                Next Generation Healthcare Platform
              </div>
              
              <h1 className="text-5xl md:text-6xl font-bold leading-tight bg-gradient-to-r from-white via-slate-200 to-slate-400 bg-clip-text text-transparent">
                Smart Healthcare <br />
                <span className="text-cyan-400">Management</span> System
              </h1>
              
              <p className="text-slate-400 text-lg max-w-lg">
                Experience the future of medical management with Royal Medico. Streamlined, secure, and accessible everywhere.
              </p>
              
              <div className="flex flex-col sm:flex-row gap-4">
                <Link 
                  to="/register" 
                  className="inline-flex items-center justify-center px-6 py-3 rounded-lg bg-cyan-500 hover:bg-cyan-600 text-white font-medium transition-colors shadow-lg shadow-cyan-500/30 group"
                >
                  Get Started 
                  <ArrowRight className="ml-2 h-4 w-4 group-hover:translate-x-1 transition-transform" />
                </Link>
                <Link 
                  to="/login" 
                  className="inline-flex items-center justify-center px-6 py-3 rounded-lg bg-white/10 hover:bg-white/20 text-white font-medium transition-colors border border-white/10"
                >
                  Live Demo
                </Link>
              </div>
            </motion.div>

            <motion.div 
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.8, delay: 0.2 }}
              className="relative"
            >
              {/* Glassmorphism Card */}
              <div className="glass-morphic p-8 rounded-2xl relative z-10">
                <div className="flex items-center justify-between mb-8">
                  <div className="flex items-center gap-4">
                    <div className="h-12 w-12 rounded-full bg-cyan-500/20 flex items-center justify-center">
                      <Activity className="h-6 w-6 text-cyan-400" />
                    </div>
                    <div>
                      <h3 className="font-semibold text-white">Health Analytics</h3>
                      <p className="text-slate-400 text-xs">Real-time data</p>
                    </div>
                  </div>
                  <div className="text-cyan-400 font-bold">+24%</div>
                </div>
                
                <div className="space-y-4">
                  <div className="h-2 bg-slate-700 rounded-full overflow-hidden">
                    <div className="h-full bg-cyan-500 rounded-full w-[70%]"></div>
                  </div>
                  <div className="h-2 bg-slate-700 rounded-full overflow-hidden">
                    <div className="h-full bg-purple-500 rounded-full w-[45%]"></div>
                  </div>
                </div>

                <div className="mt-8 grid grid-cols-2 gap-4">
                  <div className="bg-white/5 p-4 rounded-lg text-center">
                    <div className="text-sm text-slate-400">Patients</div>
                    <div className="text-xl font-bold text-white">1,234</div>
                  </div>
                  <div className="bg-white/5 p-4 rounded-lg text-center">
                    <div className="text-sm text-slate-400">Prescriptions</div>
                    <div className="text-xl font-bold text-white">5,678</div>
                  </div>
                </div>
              </div>

              {/* Floating Elements */}
              <div className="absolute -top-6 -right-6 h-16 w-16 bg-purple-500 rounded-full opacity-50 blur-xl"></div>
              <div className="absolute -bottom-6 -left-6 h-20 w-20 bg-cyan-500 rounded-full opacity-50 blur-xl"></div>
            </motion.div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-20 bg-slate-950">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-16">
            <h2 className="text-3xl md:text-4xl font-bold mb-4">Why Choose Royal Medico</h2>
            <p className="text-slate-400">
              We provide cutting-edge solutions for modern healthcare management.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="glass p-6 rounded-xl border border-white/5 hover:border-white/10 transition-colors">
              <div className="h-10 w-10 bg-cyan-500/20 rounded-lg flex items-center justify-center mb-4">
                <Shield className="h-5 w-5 text-cyan-400" />
              </div>
              <h3 className="text-lg font-semibold mb-2">Secure & Compliant</h3>
              <p className="text-slate-400 text-sm">
                Enterprise-grade security ensuring all medical data is protected.
              </p>
            </div>

            <div className="glass p-6 rounded-xl border border-white/5 hover:border-white/10 transition-colors">
              <div className="h-10 w-10 bg-purple-500/20 rounded-lg flex items-center justify-center mb-4">
                <Zap className="h-5 w-5 text-purple-400" />
              </div>
              <h3 className="text-lg font-semibold mb-2">Fast & Responsive</h3>
              <p className="text-slate-400 text-sm">
                Real-time data updates and lightning-fast page loads.
              </p>
            </div>

            <div className="glass p-6 rounded-xl border border-white/5 hover:border-white/10 transition-colors">
              <div className="h-10 w-10 bg-pink-500/20 rounded-lg flex items-center justify-center mb-4">
                <Users className="h-5 w-5 text-pink-400" />
              </div>
              <h3 className="text-lg font-semibold mb-2">User Friendly</h3>
              <p className="text-slate-400 text-sm">
                Intuitive interface designed for both doctors and patients.
              </p>
            </div>
          </div>
        </div>
      </section>

      <Footer />
    </div>
  );
};

export default Landing;
