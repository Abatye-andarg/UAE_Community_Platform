import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { User, Mail, Lock, Phone, MapPin, AlertCircle } from 'lucide-react';
import '../styles/Auth.css';

const Signup = () => {
  const { login } = useAuth();
  const [formData, setFormData] = useState({
    familyName: '',
    email: '',
    password: '',
    phone: '',
    address: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await authService.signup(formData);
      await login(formData.email, formData.password);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data || 'Registration failed. Email might already be in use.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container auth-page">
      <div className="card auth-card auth-card--wide fade-in">
        <h2 className="auth-title">Join the Community</h2>
        <p className="auth-subtitle">Create your family profile and start making a difference.</p>

        {error && (
          <div className="auth-error">
            <AlertCircle size={20} />
            {typeof error === 'string' ? error : 'Registration failed'}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="input-group">
            <label>Family Name</label>
            <div className="auth-input-icon-wrap">
              <User size={18} className="auth-input-icon" />
              <input
                name="familyName"
                type="text"
                value={formData.familyName}
                onChange={handleChange}
                required
                placeholder="John Doe Family"
              />
            </div>
          </div>

          <div className="input-group">
            <label>Email Address</label>
            <div className="auth-input-icon-wrap">
              <Mail size={18} className="auth-input-icon" />
              <input
                name="email"
                type="email"
                value={formData.email}
                onChange={handleChange}
                required
                placeholder="family@example.ae"
              />
            </div>
          </div>

          <div className="auth-two-col">
            <div className="input-group">
              <label>Phone Number</label>
              <div className="auth-input-icon-wrap">
                <Phone size={18} className="auth-input-icon" />
                <input
                  name="phone"
                  type="text"
                  value={formData.phone}
                  onChange={handleChange}
                  required
                  placeholder="050xxxxxxx"
                />
              </div>
            </div>
            <div className="input-group">
              <label>Password</label>
              <div className="auth-input-icon-wrap">
                <Lock size={18} className="auth-input-icon" />
                <input
                  name="password"
                  type="password"
                  value={formData.password}
                  onChange={handleChange}
                  required
                  placeholder="••••••••"
                />
              </div>
            </div>
          </div>

          <div className="input-group">
            <label>Physical Address (Area)</label>
            <div className="auth-input-icon-wrap">
              <MapPin size={18} className="auth-input-icon" />
              <input
                name="address"
                type="text"
                value={formData.address}
                onChange={handleChange}
                required
                placeholder="Al Reem Island, Abu Dhabi"
              />
            </div>
          </div>

          <button
            type="submit"
            className="btn btn-primary auth-submit-btn"
            disabled={loading}
          >
            {loading ? 'Creating account...' : 'Create Account'}
          </button>
        </form>

        <p className="auth-footer">
          Already have an account?{' '}
          <Link to="/login">Sign In</Link>
        </p>
      </div>
    </div>
  );
};

export default Signup;
