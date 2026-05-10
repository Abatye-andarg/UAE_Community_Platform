import React, { useEffect, useState } from 'react';
import api, { helpService } from '../services/api';
import { Link, useNavigate } from 'react-router-dom';
import { Heart, User, MessageCircle } from 'lucide-react';

const OffersView = () => {
  const navigate = useNavigate();
  const [offers, setOffers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchOffers = async () => {
      try {
        const response = await helpService.getOffers();
        setOffers(response.data);
      } catch (err) { console.error('Failed to fetch offers', err); }
      finally { setLoading(false); }
    };
    fetchOffers();
  }, []);

  const handleContact = (family) => {
    navigate(`/family/${family.id}`, { state: { family } });
  };

  if (loading) return null;

  return (
    <section id="offers-view" style={{ padding: '6rem 0', borderTop: '1px solid var(--border)' }}>
      <header style={{ marginBottom: '4rem', textAlign: 'center' }}>
        <div style={{ color: 'var(--primary)', fontWeight: 800, fontSize: '0.9rem', letterSpacing: '0.1em', marginBottom: '1rem' }}>COMMUNITY SUPPORT</div>
        <h2 style={{ fontSize: '3rem', fontWeight: 800 }}>Available Help</h2>
        <p style={{ color: 'var(--text-muted)', fontSize: '1.1rem' }}>Discover skills and services shared by your neighbors.</p>
      </header>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(360px, 1fr))', gap: '2.5rem' }}>
        {offers.map(offer => (
          <div key={offer.id} className="card" style={{ borderTop: '6px solid #10b981' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
              <span className="badge badge-success">{offer.category.name}</span>
            </div>
            <h3 style={{ fontSize: '1.5rem', marginBottom: '1rem' }}>{offer.title}</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '1rem', minHeight: '80px', marginBottom: '1.5rem' }}>{offer.description}</p>
            
            <div style={{ borderTop: '1px solid var(--border)', paddingTop: '1.5rem', marginTop: '1.5rem' }}>
              <Link to={`/family/${offer.family.id}`} state={{ family: offer.family }} style={{ textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <div style={{ width: '32px', height: '32px', background: 'var(--primary-light)', color: 'var(--primary)', borderRadius: '50%', display: 'flex', justifyContent: 'center', alignItems: 'center', fontWeight: 800, fontSize: '0.8rem' }}>
                  {offer.family.familyName.charAt(0)}
                </div>
                <span style={{ fontSize: '0.95rem', color: 'var(--text)' }}>Offered by <strong>{offer.family.familyName}</strong></span>
              </Link>
            </div>
            
            <button onClick={() => handleContact(offer.family)} className="btn btn-primary" style={{ width: '100%', marginTop: '1.5rem', justifyContent: 'center' }}>
              <MessageCircle size={20} /> Contact Family
            </button>
          </div>
        ))}
      </div>
    </section>
  );
};

export default OffersView;
