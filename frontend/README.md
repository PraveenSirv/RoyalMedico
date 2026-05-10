# Royal Medico Frontend

Premium medical management dashboard product built with React, Vite, Tailwind CSS, and Framer Motion.

## Tech Stack

- **ReactJS** (latest stable version)
- **Vite** for fast development
- **React Router DOM** for routing
- **Axios** for API requests with interceptors for token refresh
- **React Hook Form + Zod** for form validation
- **Context API** for auth state management
- **Tailwind CSS v4** for styling
- **Framer Motion** for animations
- **Lucide React** for icons
- **Sonner** for toast notifications

## Setup Instructions

1. **Navigate to the frontend directory:**
   ```bash
   cd frontend
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Create `.env` file:**
   Copy `.env.example` to `.env` and update the base URL if needed.
   ```bash
   cp .env.example .env
   ```

4. **Run the development server:**
   ```bash
   npm run dev
   ```

The application will be available at `http://localhost:5173` (or the port specified by Vite).

## Architecture

- `src/api`: Axios client and API calls
- `src/components`: Reusable UI components and layouts
- `src/context`: Auth context and state
- `src/pages`: Page components (Landing, Login, Register, Dashboards)
- `src/routes`: Routing system and route guards
- `src/types`: TypeScript types

## Authentication Flow

- JWT based authentication.
- Access tokens and refresh tokens are stored in `localStorage`.
- Automatic token refresh handled by Axios interceptors.
- Role-based access control for dashboards.
