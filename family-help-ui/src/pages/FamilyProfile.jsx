import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import api, { reputationService, taskService, feedbackService } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Star, CheckCircle, ArrowLeft, Send, MessageSquare } from 'lucide-react';
import '../styles/FamilyProfile.css';
import '../styles/Rating.css';

// ─── Helpers ─────────────────────────────────────────────────────────────────

/** Render n filled / empty stars (read-only display) */
const StarDisplay = ({ value }) => (
  <div className="feedback-stars-row">
    {[1, 2, 3, 4, 5].map(n => (
      <Star
        key={n}
        size={14}
        fill={n <= value ? 'var(--accent)' : 'none'}
        color={n <= value ? 'var(--accent)' : 'var(--border)'}
      />
    ))}
  </div>
);

/** Interactive star picker for submitting a rating */
const StarPicker = ({ value, onChange }) => (
  <div className="stars-row">
    {[1, 2, 3, 4, 5].map(n => (
      <button key={n} type="button" className="star-btn" onClick={() => onChange(n)}>
        <Star
          size={28}
          fill={n <= value ? 'var(--accent)' : 'none'}
          color={n <= value ? 'var(--accent)' : 'var(--border)'}
        />
      </button>
    ))}
  </div>
);

// ─── Component ───────────────────────────────────────────────────────────────
const FamilyProfile = () => {
  const { id }     = useParams();
  const { user }   = useAuth();
  const navigate   = useNavigate();
  const location   = useLocation();

  const [family,    setFamily]    = useState(location.state?.family || null);
  const [reputation,setReputation]= useState(null);
  const [tasks,     setTasks]     = useState([]);    // completed tasks of target family
  const [messages,  setMessages]  = useState([]);
  const [newMessage,setNewMessage]= useState('');
  const [loading,   setLoading]   = useState(true);

  // feedbackByTask: { [taskId]: Feedback[] }  — public reviews visible to everyone
  const [feedbackByTask, setFeedbackByTask] = useState({});

  // per-task form state
  const [alreadyRated,     setAlreadyRated]     = useState({});  // taskId → bool
  const [starValue,        setStarValue]         = useState({});  // taskId → 1-5
  const [commentValue,     setCommentValue]      = useState({});  // taskId → string
  const [submittingRating, setSubmittingRating]  = useState(null);

  const targetId     = Number(id);
  const currentUserId= user ? Number(user.id) : null;
  const canRate      = !!user && currentUserId !== targetId;

  // ── Fetch everything ───────────────────────────────────────────────────────
  useEffect(() => {
    const load = async () => {
      if (!targetId) return;
      try {
        setLoading(true);

        // 1. Family profile
        const familyRes = await api.get(`/families/${targetId}`);
        setFamily(familyRes.data);

        // 2. Reputation + completed tasks
        const [repRes, taskRes] = await Promise.all([
          reputationService.getReputation(targetId).catch(() => ({ data: null })),
          taskService.getTasks({ helperFamilyId: targetId }).catch(() => ({ data: [] }))
        ]);
        setReputation(repRes.data);
        const done = (taskRes.data ?? []).filter(t => t.status === 'COMPLETED');
        setTasks(done);

        // 3. For each completed task — fetch ALL public feedback
        if (done.length > 0) {
          const results = await Promise.allSettled(
            done.map(t => feedbackService.getByTask(t.id).then(r => ({ taskId: t.id, data: r.data })))
          );
          const fbMap  = {};
          const ratedMap = {};
          results.forEach(r => {
            if (r.status === 'fulfilled') {
              fbMap[r.value.taskId] = r.value.data;
              // check if current user already rated this task
              if (currentUserId) {
                ratedMap[r.value.taskId] = r.value.data.some(
                  f => Number(f.reviewerFamily.id) === currentUserId
                );
              }
            }
          });
          setFeedbackByTask(fbMap);
          setAlreadyRated(ratedMap);
        }

        // 4. Messages (only when viewing someone else's profile while logged in)
        if (currentUserId && currentUserId !== targetId) {
          const msgRes = await api.get('/messages', {
            params: { senderFamilyId: currentUserId, receiverFamilyId: targetId }
          });
          setMessages(msgRes.data);
          await api.post('/messages/mark-as-read', null, {
            params: { receiverId: currentUserId, senderId: targetId }
          });
          window.dispatchEvent(new CustomEvent('REFRESH_NOTIFICATIONS'));
        }
      } catch (err) {
        console.error('Failed to load profile:', err);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [targetId, currentUserId]);

  // ── Handlers ───────────────────────────────────────────────────────────────
  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!newMessage.trim()) return;
    try {
      const res = await api.post('/messages', {
        senderFamily:   { id: currentUserId },
        receiverFamily: { id: targetId },
        messageText:    newMessage,
        isRead: false,
        helpTask: null
      });
      setMessages(prev => [...prev, res.data]);
      setNewMessage('');
    } catch (err) {
      alert('Message failed: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleSubmitRating = async (task) => {
    const rating = starValue[task.id];
    if (!rating) { alert('Please select a star rating first.'); return; }
    setSubmittingRating(task.id);
    try {
      const res = await feedbackService.submit({
        rating,
        comment:        commentValue[task.id] || '',
        helpTask:       { id: task.id },
        reviewerFamily: { id: currentUserId },
        targetFamily:   { id: targetId }
      });
      // Append new review to the public list immediately (optimistic)
      setFeedbackByTask(prev => ({
        ...prev,
        [task.id]: [...(prev[task.id] || []), res.data]
      }));
      setAlreadyRated(prev => ({ ...prev, [task.id]: true }));
      // Refresh trust score
      const repRes = await reputationService.getReputation(targetId).catch(() => ({ data: null }));
      setReputation(repRes.data);
    } catch (err) {
      alert('Failed to submit rating: ' + (err.response?.data?.message || err.message));
    } finally {
      setSubmittingRating(null);
    }
  };

  // ── Render ─────────────────────────────────────────────────────────────────
  if (loading) return <div className="container profile-loading">Opening conversation...</div>;

  return (
    <div className="container fade-in profile-page">
      <button onClick={() => navigate(-1)} className="btn btn-secondary profile-back-btn">
        <ArrowLeft size={18} /> Back
      </button>

      <div className="profile-layout">

        {/* ── Sidebar ──────────────────────────────────── */}
        <aside>
          <div className="card profile-card">
            <div className="profile-avatar">
              {family?.familyName ? family.familyName.charAt(0).toUpperCase() : '?'}
            </div>
            <h2 className="profile-name">{family?.familyName || 'Family Member'}</h2>
            <p className="profile-role">Community Member</p>

            <div className="profile-stats-grid">
              <div className="card profile-stat-card">
                <Star color="var(--accent)" size={24} fill="var(--accent)" style={{ marginBottom: '0.25rem' }} />
                <div className="profile-stat-value">{reputation?.reliabilityScore?.toFixed(1) || '0.0'}</div>
                <div className="profile-stat-label">Trust Score</div>
              </div>
              <div className="card profile-stat-card">
                <CheckCircle color="var(--primary)" size={24} style={{ marginBottom: '0.25rem' }} />
                <div className="profile-stat-value">{reputation?.completedTasks || '0'}</div>
                <div className="profile-stat-label">Tasks</div>
              </div>
            </div>
          </div>
        </aside>

        {/* ── Main ─────────────────────────────────────── */}
        <section>

          {/* Chat — only when viewing someone else's profile */}
          {currentUserId && currentUserId !== targetId && (
            <div className="card chat-container" style={{ marginBottom: '2rem' }}>
              <div className="chat-header">
                <MessageSquare size={20} color="var(--primary)" />
                <h3 className="chat-header-title">Chat with {family?.familyName || 'Family'}</h3>
              </div>

              <div className="chat-messages">
                {messages.length > 0 ? (
                  messages.map((msg, i) => {
                    const isSent = Number(msg.senderFamily.id) === currentUserId;
                    return (
                      <div key={i} className={`chat-bubble ${isSent ? 'chat-bubble--sent' : 'chat-bubble--received'}`}>
                        <div className="chat-bubble-text">{msg.messageText}</div>
                        <div className="chat-bubble-time">
                          {new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </div>
                      </div>
                    );
                  })
                ) : (
                  <div className="chat-empty">
                    <MessageSquare size={48} className="chat-empty-icon" />
                    <div>No previous messages. Start a conversation with {family?.familyName}!</div>
                  </div>
                )}
              </div>

              <form onSubmit={handleSendMessage} className="chat-form">
                <input
                  className="chat-input"
                  value={newMessage}
                  onChange={e => setNewMessage(e.target.value)}
                  placeholder="Type your message here..."
                />
                <button type="submit" className="btn btn-primary chat-send-btn">
                  <Send size={20} />
                </button>
              </form>
            </div>
          )}

          {/* Completed tasks + public feedback + rating form */}
          <div>
            <h3 className="contributions-title">
              {tasks.length > 0 ? 'Completed Tasks & Community Feedback' : 'Contributions'}
            </h3>

            <div className="contributions-list">
              {tasks.length > 0 ? tasks.map(task => {
                const taskFeedback = feedbackByTask[task.id] || [];
                return (
                  <div key={task.id} className="card contribution-card"
                    style={{ flexDirection: 'column', alignItems: 'stretch', gap: 0 }}
                  >
                    {/* Task header row */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <div className="contribution-category">
                          {task.helpRequest?.title || task.helpOffer?.title || (task.category?.name + ' Support')}
                        </div>
                        <div className="contribution-date">
                          Completed {new Date(task.createdAt).toLocaleDateString()}
                        </div>
                      </div>
                      <span className="badge badge-success">COMPLETED</span>
                    </div>

                    {/* ── Public feedback — visible to EVERYONE including profile owner ── */}
                    <div className="rating-section">
                      <div className="rating-section-title">
                        Community Feedback ({taskFeedback.length})
                      </div>

                      {taskFeedback.length > 0 ? (
                        <div className="feedback-list">
                          {taskFeedback.map((fb, i) => (
                            <div key={i} className="feedback-item">
                              <div className="feedback-item-header">
                                <div className="feedback-reviewer">
                                  <div className="feedback-reviewer-avatar">
                                    {fb.reviewerFamily?.familyName?.charAt(0)?.toUpperCase() || '?'}
                                  </div>
                                  <span className="feedback-reviewer-name">
                                    {fb.reviewerFamily?.familyName || 'Anonymous'}
                                  </span>
                                </div>
                                <StarDisplay value={fb.rating} />
                              </div>
                              {fb.comment && (
                                <p className="feedback-comment">"{fb.comment}"</p>
                              )}
                            </div>
                          ))}
                        </div>
                      ) : (
                        <p className="feedback-no-reviews">
                          No reviews yet.{canRate ? ' Be the first to rate this task!' : ''}
                        </p>
                      )}

                      {/* Rating form — only for logged-in visitors who are NOT the profile owner */}
                      {canRate && (
                        <>
                          <hr className="rating-form-divider" />
                          {alreadyRated[task.id] ? (
                            <div className="rating-already-done">
                              <CheckCircle size={16} /> You already rated this task
                            </div>
                          ) : (
                            <>
                              <div className="rating-form-label">
                                <Star size={15} color="var(--accent)" fill="var(--accent)" />
                                Leave your rating for {family?.familyName}
                              </div>
                              <StarPicker
                                value={starValue[task.id] || 0}
                                onChange={v => setStarValue(prev => ({ ...prev, [task.id]: v }))}
                              />
                              <textarea
                                className="rating-comment"
                                rows={2}
                                placeholder="Optional comment..."
                                value={commentValue[task.id] || ''}
                                onChange={e => setCommentValue(prev => ({ ...prev, [task.id]: e.target.value }))}
                              />
                              <button
                                className="btn btn-primary rating-submit-btn"
                                onClick={() => handleSubmitRating(task)}
                                disabled={submittingRating === task.id || !starValue[task.id]}
                              >
                                <Star size={15} />
                                {submittingRating === task.id ? 'Submitting...' : 'Submit Rating'}
                              </button>
                            </>
                          )}
                        </>
                      )}
                    </div>
                  </div>
                );
              }) : (
                <div className="card contributions-empty">
                  This family is ready to start helping!
                </div>
              )}
            </div>
          </div>

        </section>
      </div>
    </div>
  );
};

export default FamilyProfile;
