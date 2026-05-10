import React, { useState, useEffect } from 'react';
import { NavLink, Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import { Heart, LogOut, Sun, Moon, Bell } from 'lucide-react';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [theme, setTheme] = useState(localStorage.getItem('theme') || 'light');
  const [activeScrollSection, setActiveScrollSection] = useState('home');
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
  }, [theme]);

  const fetchUnreadCount = async () => {
    if (!user) return;
    try {
      const res = await api.get('/messages/unread-count', {
        params: { familyId: user.id }
      });
      setUnreadCount(res.data);
    } catch (err) {
      console.error('Failed to fetch unread count', err);
    }
  };

  useEffect(() => {
    fetchUnreadCount();
    const interval = setInterval(fetchUnreadCount, 10000);

    // Instant Refresh Listener
    window.addEventListener('REFRESH_NOTIFICATIONS', fetchUnreadCount);

    return () => {
      clearInterval(interval);
      window.removeEventListener('REFRESH_NOTIFICATIONS', fetchUnreadCount);
    };
  }, [user]);

  useEffect(() => {
    if (location.pathname !== '/' || !user) return;

    const handleScroll = () => {
      const sections = [
        { id: 'home-hero', label: 'home' },
        { id: 'requests-view', label: 'requests' },
        { id: 'offers-view', label: 'offers' },
        { id: 'dashboard-view', label: 'dashboard' }
      ];

      for (const section of sections) {
        const element = document.getElementById(section.id);
        if (element) {
          const rect = element.getBoundingClientRect();
          if (rect.top <= 160 && rect.bottom >= 160) {
            setActiveScrollSection(section.label);
            break;
          }
        }
      }
    };

    window.addEventListener('scroll', handleScroll);
    handleScroll();
    return () => window.removeEventListener('scroll', handleScroll);
  }, [location.pathname, user]);

  const toggleTheme = () => {
    setTheme(theme === 'light' ? 'dark' : 'light');
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isHome = location.pathname === '/';

  const getCustomClass = (path, label) => {
    if (isHome && user) {
      return activeScrollSection === label ? 'active' : '';
    }
    return '';
  };

  return (
    <nav className="navbar">
      <div className="container nav-content">
        <Link to="/" className="logo">
          <Heart fill="#ef4444" size={32} color="#ef4444" />
          <span>FamilyHelpUAE</span>
        </Link>
        
        <div className="nav-links">
          <NavLink to="/" end className={({ isActive }) => (isActive && getCustomClass('/', 'home') === 'active') || (isActive && !user) ? 'active' : getCustomClass('/', 'home')}>
            Home
          </NavLink>
          
          {user ? (
            <>
              <NavLink to="/requests" className={({ isActive }) => (isActive && !isHome) ? 'active' : getCustomClass('/requests', 'requests')}>
                Requests
              </NavLink>
              <NavLink to="/offers" className={({ isActive }) => (isActive && !isHome) ? 'active' : getCustomClass('/offers', 'offers')}>
                Offers
              </NavLink>
              <NavLink to="/dashboard" className={({ isActive }) => (isActive && !isHome) ? 'active' : getCustomClass('/dashboard', 'dashboard')}>
                Dashboard
              </NavLink>
              
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginLeft: '1rem' }}>
                <div 
                  onClick={() => navigate('/inbox')}
                  className="nav-icon-btn"
                  style={{ position: 'relative', cursor: 'pointer', display: 'flex', alignItems: 'center', padding: '8px', borderRadius: '50%', transition: 'background 0.2s' }} 
                  title="Messages & Notifications"
                >
                   <Bell size={22} color="var(--text)" />
                   {unreadCount > 0 && (
                     <span style={{ 
                       position: 'absolute', 
                       top: '-4px', 
                       right: '-4px', 
                       background: '#ef4444', 
                       color: 'white', 
                       fontSize: '0.65rem', 
                       fontWeight: 900, 
                       padding: '2px 6px', 
                       borderRadius: '10px',
                       border: '2px solid var(--surface)',
                       boxShadow: 'var(--shadow-sm)'
                     }}>
                       {unreadCount > 9 ? '9+' : unreadCount}
                     </span>
                   )}
                </div>

                <button onClick={toggleTheme} className="theme-toggle">
                  {theme === 'light' ? <Moon size={18} /> : <Sun size={18} />}
                </button>
                <button onClick={handleLogout} className="btn btn-secondary" style={{ padding: '0.5rem 1rem', color: '#ef4444', borderColor: 'var(--border)' }}>
                  <LogOut size={16} /> Logout
                </button>
              </div>
            </>
          ) : (
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <button onClick={toggleTheme} className="theme-toggle">
                {theme === 'light' ? <Moon size={18} /> : <Sun size={18} />}
              </button>
              <NavLink to="/login">Login</NavLink>
              <Link to="/signup" className="btn btn-primary" style={{ padding: '0.6rem 1.4rem', lineHeight: 1 }}>Sign Up</Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
