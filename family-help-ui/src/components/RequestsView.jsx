import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api, { helpService, taskService } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { AlertCircle, User, HandHeart } from 'lucide-react';

const RequestsView = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchRequests = async () => {
      try {
        const response = await helpService.getRequests();
        setRequests(response.data);
      } catch (err) { console.error('Failed to fetch requests', err); }
      finally { setLoading(false); }
    };
    fetchRequests();
  }, []);

  const handleHelp = async (request) => {
    if (!user) { navigate('/login'); return; }
    
    // Safety check for self-help
    if (Number(user.id) === Number(request.family.id)) {
      alert("You cannot accept your own request.");
      return;
    }

    try {
      setLoading(true);
      await taskService.createTask({
        helpRequest: { id: request.id },
        category: { id: request.category.id },
        requesterFamily: { id: request.family.id },
        helperFamily: { id: user.id },
        status: 'ACTIVE'
      });
      alert('Success! You are now helping ' + request.family.familyName + '. Check your dashboard for details.');
      navigate('/home');
    } catch (err) {
      console.error('Task creation failed', err);
      alert('Failed to accept request: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  if (loading && requests.length === 0) return <div style={{ padding: '4rem', textAlign: 'center' }}>Loading requests...</div>;

  return (
    <section id="requests-view" style={{ padding: '6rem 0', borderTop: '1px solid var(--border)' }}>
      <header style={{ marginBottom: '4rem', textAlign: 'center' }}>
        <div style={{ color: 'var(--primary)', fontWeight: 800, fontSize: '0.9rem', letterSpacing: '0.1em', marginBottom: '1rem' }}>NEIGHBORHOOD NEED</div>
        <h2 style={{ fontSize: '3rem', fontWeight: 800 }}>Help Needed</h2>
        <p style={{ color: 'var(--text-muted)', fontSize: '1.1rem' }}>Small acts of kindness make the biggest difference.</p>
      </header>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(360px, 1fr))', gap: '2.5rem' }}>
        {requests.map(request => (
          <div key={request.id} className="card" style={{ borderTop: `6px solid ${request.urgency === 'URGENT' ? '#ef4444' : 'var(--primary)'}` }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
              <span className="badge badge-info">{request.category.name}</span>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', fontSize: '0.75rem', fontWeight: 800, color: request.urgency === 'URGENT' ? '#ef4444' : 'var(--text-muted)' }}>
                <AlertCircle size={14} /> 
                {request.urgency === 'URGENT' ? 'Critical Urgent' : 
                 request.urgency === 'HIGH' ? 'High Urgent' : 
                 request.urgency === 'NORMAL' ? 'Normal Urgent' : 'Low Urgent'}
              </div>
            </div>
            <h3 style={{ fontSize: '1.5rem', marginBottom: '1rem' }}>{request.title}</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '1rem', minHeight: '80px', marginBottom: '1.5rem' }}>{request.description}</p>
            
            <div style={{ borderTop: '1px solid var(--border)', paddingTop: '1.5rem', marginTop: '1.5rem' }}>
              <Link to={`/family/${request.family.id}`} state={{ family: request.family }} style={{ textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <div style={{ width: '32px', height: '32px', background: 'var(--primary-light)', color: 'var(--primary)', borderRadius: '50%', display: 'flex', justifyContent: 'center', alignItems: 'center', fontWeight: 800, fontSize: '0.8rem' }}>
                  {request.family.familyName.charAt(0)}
                </div>
                <span style={{ fontSize: '0.95rem', color: 'var(--text)' }}>Requested by <strong>{request.family.familyName}</strong></span>
              </Link>
            </div>
            
            <button 
              onClick={() => handleHelp(request)} 
              className="btn btn-primary" 
              style={{ width: '100%', marginTop: '1.5rem', justifyContent: 'center' }}
              disabled={loading}
            >
              <HandHeart size={20} /> {loading ? 'Processing...' : 'I Can Help!'}
            </button>
          </div>
        ))}
      </div>
    </section>
  );
};

export default RequestsView;
