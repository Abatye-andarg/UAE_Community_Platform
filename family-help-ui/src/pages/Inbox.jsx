import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { MessageSquare, ArrowRight, Clock } from 'lucide-react';
import '../styles/Inbox.css';

const Inbox = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [conversations, setConversations] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) return;
    const fetchConversations = async () => {
      try {
        const res = await api.get('/messages/conversations', {
          params: { familyId: user.id }
        });
        setConversations(res.data);
      } catch (err) {
        console.error('Failed to fetch conversations', err);
      } finally {
        setLoading(false);
      }
    };
    fetchConversations();
  }, [user]);

  if (loading) return <div className="container inbox-loading">Loading your messages...</div>;

  return (
    <div className="container fade-in inbox-page">
      <div className="inbox-header">
        <MessageSquare size={32} color="var(--primary)" />
        <h1 className="inbox-title">Your Conversations</h1>
      </div>

      {conversations.length > 0 ? (
        <div className="inbox-list">
          {conversations.map((conv) => {
            const otherFamily = conv.senderFamily.id === user.id ? conv.receiverFamily : conv.senderFamily;
            const isUnread = conv.isRead === false && conv.receiverFamily.id === user.id;
            return (
              <div
                key={conv.id}
                className={`card inbox-item ${isUnread ? 'inbox-item--unread' : 'inbox-item--read'}`}
                onClick={() => navigate(`/family/${otherFamily.id}`)}
              >
                <div className="inbox-item-left">
                  <div className="inbox-avatar">
                    {otherFamily.familyName.charAt(0).toUpperCase()}
                  </div>
                  <div>
                    <div className="inbox-family-name">{otherFamily.familyName}</div>
                    <div className="inbox-preview">
                      {conv.senderFamily.id === user.id ? 'You: ' : ''}
                      <span className="inbox-preview-text">{conv.messageText}</span>
                    </div>
                  </div>
                </div>

                <div className="inbox-item-right">
                  <div className="inbox-date">
                    <Clock size={12} /> {new Date(conv.createdAt).toLocaleDateString()}
                  </div>
                  <ArrowRight size={20} color="var(--primary)" />
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        <div className="card inbox-empty">
          <MessageSquare size={64} className="inbox-empty-icon" />
          <h3>No conversations yet</h3>
          <p>Browse the community and reach out to other families!</p>
          <button onClick={() => navigate('/')} className="btn btn-primary inbox-empty-cta">
            Explore Families
          </button>
        </div>
      )}
    </div>
  );
};

export default Inbox;
