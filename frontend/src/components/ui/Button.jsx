import React from 'react';
import { cn } from '../../lib/utils';

export const Button = ({ 
  className, 
  variant = 'primary', 
  children, 
  ...props 
}) => {
  return (
    <button
      className={cn(
        "px-4 py-2 rounded-lg font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-cyan-500 focus:ring-offset-2 focus:ring-offset-slate-900 flex items-center justify-center gap-2",
        variant === 'primary' && "bg-cyan-500 hover:bg-cyan-600 text-white shadow-lg shadow-cyan-500/30",
        variant === 'secondary' && "bg-slate-700 hover:bg-slate-600 text-white",
        variant === 'outline' && "border border-white/10 hover:bg-white/5 text-white",
        className
      )}
      {...props}
    >
      {children}
    </button>
  );
};
