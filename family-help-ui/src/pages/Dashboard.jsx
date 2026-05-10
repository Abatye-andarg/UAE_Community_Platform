import React from 'react';
import DashboardView from '../components/DashboardView';

const Dashboard = () => {
  return (
    <div className="container fade-in" style={{ padding: '2rem 0' }}>
      <header style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2.5rem', textAlign: 'center' }}>Manage your offers, requests, and reputation.</h1>
      </header>
      <DashboardView />
    </div>
  );
};

export default Dashboard;
