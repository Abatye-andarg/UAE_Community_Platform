import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ShieldCheck, Users, Award, ArrowDown, Heart, CheckCircle, Star } from 'lucide-react';
import DashboardView from '../components/DashboardView';
import RequestsView from '../components/RequestsView';
import OffersView from '../components/OffersView';
import '../styles/Home.css';

const Home = () => {
  const { user } = useAuth();

  if (user) {
    return (
      <div className="home-logged-in fade-in">

        {/* Hero */}
        <section id="home-hero" className="container home-hero">
          <div className="home-hub-badge">YOUR LOCAL HUB</div>
          <h1 className="home-welcome-title">Welcome, {user.familyName}!</h1>
          <p className="home-welcome-subtitle">
            The community is active today. Neighbors in your area are looking for support and sharing their skills.
          </p>
          <div
            className="home-scroll-arrow"
            onClick={() => document.getElementById('requests-view').scrollIntoView({ behavior: 'smooth' })}
          >
            <ArrowDown size={48} />
          </div>
        </section>

        {/* Family Spotlight */}
        <section className="home-spotlight-section">
          <div className="container">
            <div className="home-spotlight-grid">
              <div>
                <div className="home-spotlight-label">
                  <Star size={20} fill="var(--primary)" /> FAMILY SPOTLIGHT
                </div>
                <h2 className="home-spotlight-title">Making a Difference: The Al-Suwaidi Family</h2>
                <p className="home-spotlight-desc">
                  Last week, the Al-Suwaidi family helped 4 elderly neighbors with grocery runs and technical support.
                  They've earned the "Golden Neighbor" badge for their consistent kindness.
                </p>
                <div className="home-spotlight-stats">
                  <span className="home-spotlight-stat">
                    <CheckCircle size={18} color="var(--primary)" /> 42 Tasks Done
                  </span>
                  <span className="home-spotlight-stat">
                    <Heart size={18} color="#ef4444" /> 15 Shoutouts
                  </span>
                </div>
              </div>
              <div className="home-spotlight-img-wrap">
                <img
                  src="https://images.unsplash.com/photo-1511895426328-dc8714191300?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80"
                  alt="Spotlight Family"
                  className="home-spotlight-img"
                />
                <div className="home-spotlight-badge">
                  <div className="home-spotlight-badge-label">RELIABILITY</div>
                  <div className="home-spotlight-badge-value">100% Verified</div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Shoutouts */}
        <section className="home-shoutouts-section">
          <div className="container">
            <h2 className="home-shoutouts-title">Recent Neighborhood Shoutouts</h2>
            <div className="home-shoutouts-grid">
              {[
                { initial: 'M', name: "John's Family", time: '2 hours ago in Dubai Marina', quote: '"Thank you to the Smith family for helping us with the heavy lifting today. Such a great community!"', color: 'green' },
                { initial: 'L', name: "Lopez's Family",   time: '5 hours ago in Al Reem',     quote: '"Just shared some home-grown vegetables with the family next door. Love this platform!"',          color: 'blue' },
                { initial: 'A', name: "Ahmed's Family",   time: 'Yesterday in Sharjah',        quote: '"Grateful for the school pickup help. It really saved our afternoon!"',                            color: 'yellow' },
              ].map(({ initial, name, time, quote, color }) => (
                <div key={name} className="card home-shoutout-card">
                  <div className="home-shoutout-header">
                    <div className={`home-shoutout-avatar home-shoutout-avatar--${color}`}>{initial}</div>
                    <div>
                      <div className="home-shoutout-name">{name}</div>
                      <div className="home-shoutout-time">{time}</div>
                    </div>
                  </div>
                  <p className="home-shoutout-quote">{quote}</p>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* Marketplace sections */}
        <div className="container">
          <RequestsView />
          <OffersView />
          <DashboardView />
        </div>
      </div>
    );
  }

  // Guest view
  return (
    <div className="fade-in">
      <section className="hero home-guest-hero">
        <div className="container">
          <h1 className="home-guest-title">Care, Support, and Togetherness</h1>
          <p className="home-guest-subtitle">
            The #1 platform for UAE families to foster solidarity, request support, and build a stronger local community.
          </p>
          <div className="home-guest-cta">
            <Link to="/signup" className="btn btn-primary">Join the Initiative</Link>
          </div>
        </div>
      </section>

      <section className="container home-stats-section">
        <div className="home-stats-grid">
          <div className="card home-stat-card">
            <div className="home-stat-value home-stat-value--green">12</div>
            <div className="home-stat-label">Active Requests Nearby</div>
          </div>
          <div className="card home-stat-card">
            <div className="home-stat-value home-stat-value--purple">24</div>
            <div className="home-stat-label">Help Offers Today</div>
          </div>
          <div className="card home-stat-card">
            <div className="home-stat-value home-stat-value--amber">4.9/5</div>
            <div className="home-stat-label">Community Trust Score</div>
          </div>
        </div>
        <div
          className="home-scroll-arrow"
          onClick={() => document.getElementById('why-section')?.scrollIntoView({ behavior: 'smooth' })}
        >
          <ArrowDown size={48} />
        </div>
      </section>

      <section id="why-section" className="container home-why-section">
        <h2 className="home-why-title">Why Join FamilyHelpUAE?</h2>
        <div className="home-features-grid">
          <div className="card home-feature-card">
            <div className="home-feature-icon home-feature-icon--green">
              <ShieldCheck size={40} color="var(--primary)" />
            </div>
            <h3>Secure &amp; Trusted</h3>
            <p className="home-feature-desc">Verified family profiles and a robust reputation system for your peace of mind.</p>
          </div>
          <div className="card home-feature-card">
            <div className="home-feature-icon home-feature-icon--blue">
              <Users size={40} color="#0284c7" />
            </div>
            <h3>Community Focused</h3>
            <p className="home-feature-desc">Specifically designed for the cultural values and neighborly support of the UAE.</p>
          </div>
          <div className="card home-feature-card">
            <div className="home-feature-icon home-feature-icon--amber">
              <Award size={40} color="var(--accent)" />
            </div>
            <h3>Reputation System</h3>
            <p className="home-feature-desc">Build your status as a pillar of the community by helping those around you.</p>
          </div>
         
        </div>

         <div className="home-guest-cta" style={{ display: 'flex', justifyContent: 'center', marginTop: '2rem' }}>
            <Link to="/signup" className="btn btn-primary">Join the Initiative</Link>
          </div>
      </section>
    </div>
  );
};

export default Home;
