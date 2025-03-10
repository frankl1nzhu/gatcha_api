import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/lib/locale/zh_CN';

// Page Components
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import PlayerPage from './pages/PlayerPage';
import BattlePage from './pages/BattlePage';
import SummoningPage from './pages/SummoningPage';
import RoyalRumblePage from './pages/RoyalRumblePage';

// Layout Components
import MainLayout from './components/MainLayout';

// Styles
import './App.css';

const App: React.FC = () => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  
  useEffect(() => {
    // Check if token exists in local storage
    const token = localStorage.getItem('token');
    if (token) {
      setIsAuthenticated(true);
    }
  }, []);

  // Login success handler
  const handleLoginSuccess = (token: string) => {
    localStorage.setItem('token', token);
    setIsAuthenticated(true);
  };

  // Logout handler
  const handleLogout = () => {
    localStorage.removeItem('token');
    setIsAuthenticated(false);
  };

  return (
    <ConfigProvider locale={zhCN}>
      <Router>
        <Routes>
          {/* Public routes */}
          <Route 
            path="/login" 
            element={
              isAuthenticated ? 
                <Navigate to="/dashboard" replace /> : 
                <Login onLoginSuccess={handleLoginSuccess} />
            } 
          />
          
          {/* Protected routes */}
          <Route 
            path="/" 
            element={
              isAuthenticated ? 
                <MainLayout onLogout={handleLogout} /> : 
                <Navigate to="/login" replace />
            }
          >
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/player" element={<PlayerPage />} />
            <Route path="/battle" element={<BattlePage />} />
            <Route path="/summoning" element={<SummoningPage />} />
            <Route path="/royal-rumble" element={<RoyalRumblePage />} />
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
          </Route>
        </Routes>
      </Router>
    </ConfigProvider>
  );
};

export default App;