import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Dashboard from './pages/Dashboard';
import Offers from './pages/Offers';
import Requests from './pages/Requests';
import FamilyProfile from './pages/FamilyProfile';
import Inbox from './pages/Inbox';

const PrivateRoute = ({ children }) => {
  const { user } = useAuth();
  return user ? children : <Navigate to="/login" />;
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <Navbar />
        <main className="fade-in">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />} />
            <Route path="/offers" element={<Offers />} />
            <Route path="/requests" element={<Requests />} />
            <Route path="/family/:id" element={<FamilyProfile />} />
            <Route 
              path="/dashboard" 
              element={
                <PrivateRoute>
                  <Dashboard />
                </PrivateRoute>
              } 
            />
            <Route 
              path="/inbox" 
              element={
                <PrivateRoute>
                  <Inbox />
                </PrivateRoute>
              } 
            />
          </Routes>
        </main>
        <footer style={{ marginTop: 'auto', padding: '2rem 0', borderTop: '1px solid var(--border)', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
          &copy; 2026 FamilyHelpUAE. Supporting UAE Families for the "Year of Family".
        </footer>
      </Router>
    </AuthProvider>
  );
}

export default App;
