import React, { useEffect, useState } from 'react';
import api, { reputationService, taskService, categoryService, helpService } from '../services/api';
import { Star, CheckCircle, TrendingUp, MessageSquare, Plus, Mail, Trash2, AlertTriangle, ClipboardCheck, User } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

const DashboardView = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [reputation, setReputation] = useState(null);
  const [tasks, setTasks] = useState([]);
  const [categories, setCategories] = useState([]);
  const [unreadMessages, setUnreadMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showOfferModal, setShowOfferModal] = useState(false);
  const [showRequestModal, setShowRequestModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const [newOffer, setNewOffer] = useState({ title: '', description: '', categoryId: '' });
  const [newRequest, setNewRequest] = useState({ title: '', description: '', categoryId: '', urgency: 'NORMAL' });

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [rep, tsk, cat, msg] = await Promise.allSettled([
          reputationService.getReputation(user.id),
          taskService.getTasks(),
          categoryService.getAll(),
          api.get('/messages', { params: { receiverFamilyId: user.id } })
        ]);
        if (rep.status === 'fulfilled') setReputation(rep.value.data);
        if (tsk.status === 'fulfilled') setTasks(tsk.value.data);
        if (cat.status === 'fulfilled') setCategories(cat.value.data);
        if (msg.status === 'fulfilled') setUnreadMessages(msg.value.data.filter(m => !m.isRead));
      } catch (err) { console.error(err); }
      finally { setLoading(false); }
    };
    fetchData();
  }, [user.id]);

  const handleCreateOffer = async (e) => {
    e.preventDefault();
    try {
      await helpService.createOffer({ family: { id: user.id }, category: { id: newOffer.categoryId }, title: newOffer.title, description: newOffer.description });
      setShowOfferModal(false);
      alert('Help offer posted successfully!');
      window.location.reload(); // Refresh to show new data
    } catch (err) { alert('Error: ' + err.message); }
  };

  const handleCreateRequest = async (e) => {
    e.preventDefault();
    try {
      await helpService.createRequest({ family: { id: user.id }, category: { id: newRequest.categoryId }, title: newRequest.title, description: newRequest.description, urgency: newRequest.urgency });
      setShowRequestModal(false);
      alert('Help request posted successfully!');
      window.location.reload();
    } catch (err) { alert('Error: ' + err.message); }
  };

  const handleDeleteAccount = async () => {
    try {
      await api.delete(`/families/${user.id}`);
      alert('Your account and all associated data have been permanently deleted.');
      logout();
      navigate('/');
    } catch (err) { alert('Failed to delete account: ' + err.message); }
  };

  const handleCompleteTask = async (taskId) => {
    if (!window.confirm('Mark this task as completed?')) return;
    try {
      await taskService.completeTask(taskId);
      setTasks(prev => prev.map(t => t.id === taskId ? { ...t, status: 'COMPLETED' } : t));
    } catch (err) {
      alert('Could not complete task: ' + (err.response?.data?.message || err.message));
    }
  };

  if (loading) return <div style={{ padding: '4rem', textAlign: 'center', color: 'var(--primary)' }}>Loading your dashboard...</div>;

  return (
    <section id="dashboard-view" style={{ padding: '4rem 0' }}>

      {/* OFFER MODAL */}
      {showOfferModal && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(15, 23, 42, 0.7)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000, backdropFilter: 'blur(4px)' }}>
          <div className="card fade-in" style={{ maxWidth: '500px', width: '90%', borderTop: '8px solid var(--primary)' }}>
            <h3 style={{ marginBottom: '1.5rem' }}>What can you offer?</h3>
            <form onSubmit={handleCreateOffer}>
              <div className="input-group">
                <label>Title</label>
                <input required value={newOffer.title} onChange={e => setNewOffer({ ...newOffer, title: e.target.value })} placeholder="e.g. Grocery Shopping Help" />
              </div>
              <div className="input-group">
                <label>Category</label>
                <select required value={newOffer.categoryId} onChange={e => setNewOffer({ ...newOffer, categoryId: e.target.value })}>
                  <option value="">Select Category</option>
                  {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>
              <div className="input-group">
                <label>Description</label>
                <textarea required value={newOffer.description} onChange={e => setNewOffer({ ...newOffer, description: e.target.value })} placeholder="Explain how you can help..." style={{ minHeight: '120px' }} />
              </div>
              <div style={{ display: 'flex', gap: '1rem' }}>
                <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>Post Offer</button>
                <button type="button" onClick={() => setShowOfferModal(false)} className="btn btn-secondary" style={{ flex: 1 }}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* REQUEST MODAL */}
      {showRequestModal && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(15, 23, 42, 0.7)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000, backdropFilter: 'blur(4px)' }}>
          <div className="card fade-in" style={{ maxWidth: '500px', width: '90%', borderTop: '8px solid #3b82f6' }}>
            <h3 style={{ marginBottom: '1.5rem' }}>What do you need?</h3>
            <form onSubmit={handleCreateRequest}>
              <div className="input-group">
                <label>Title</label>
                <input required value={newRequest.title} onChange={e => setNewRequest({ ...newRequest, title: e.target.value })} placeholder="e.g. Need help with School Pickup" />
              </div>
              <div className="input-group">
                <label>Category</label>
                <select required value={newRequest.categoryId} onChange={e => setNewRequest({ ...newRequest, categoryId: e.target.value })}>
                  <option value="">Select Category</option>
                  {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>
              <div className="input-group">
                <label>Urgency Level</label>
                <select value={newRequest.urgency} onChange={e => setNewRequest({ ...newRequest, urgency: e.target.value })}>
                  <option value="LOW">Low Urgent</option>
                  <option value="NORMAL">Normal Urgent</option>
                  <option value="HIGH">High Urgent</option>
                  <option value="URGENT">Critical Urgent</option>
                </select>
              </div>
              <div className="input-group">
                <label>Description</label>
                <textarea required value={newRequest.description} onChange={e => setNewRequest({ ...newRequest, description: e.target.value })} placeholder="Explain your request..." style={{ minHeight: '120px' }} />
              </div>
              <div style={{ display: 'flex', gap: '1rem' }}>
                <button type="submit" className="btn btn-primary" style={{ flex: 1, background: '#3b82f6' }}>Post Request</button>
                <button type="button" onClick={() => setShowRequestModal(false)} className="btn btn-secondary" style={{ flex: 1 }}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* DELETE MODAL */}
      {showDeleteModal && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(220, 38, 38, 0.2)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000, backdropFilter: 'blur(8px)' }}>
          <div className="card fade-in" style={{ maxWidth: '450px', width: '90%', textAlign: 'center', border: '2px solid #ef4444' }}>
            <AlertTriangle size={48} color="#ef4444" style={{ marginBottom: '1rem' }} />
            <h3 style={{ color: '#ef4444', marginBottom: '1rem' }}>Delete Account Permanently?</h3>
            <p style={{ color: 'var(--text-muted)', marginBottom: '2rem' }}>
              This action cannot be undone. All your offers, requests, messages, and reputation points will be deleted forever.
            </p>
            <div style={{ display: 'flex', gap: '1rem' }}>
              <button onClick={handleDeleteAccount} className="btn btn-primary" style={{ flex: 1, background: '#ef4444' }}>Yes, Delete Everything</button>
              <button onClick={() => setShowDeleteModal(false)} className="btn btn-secondary" style={{ flex: 1 }}>Cancel</button>
            </div>
          </div>
        </div>
      )}

      {unreadMessages.length > 0 && (
        <div className="card fade-in" style={{ background: 'var(--gradient)', color: 'white', marginBottom: '3rem', border: 'none' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
              <Mail size={24} />
              <div>
                <div style={{ fontWeight: 800, fontSize: '1.2rem' }}>{unreadMessages.length} New Messages</div>
                <div style={{ opacity: 0.9 }}>Check family profiles to reply and earn reputation!</div>
              </div>
            </div>
            <button onClick={() => navigate('/inbox')} className="btn btn-secondary" style={{ border: 'none', fontWeight: 700 }}>Open Chat</button>
          </div>
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '2rem', marginBottom: '4rem' }}>
        <div className="card" style={{ textAlign: 'center' }}>
          <Star color="var(--accent)" fill="var(--accent)" size={28} style={{ marginBottom: '1rem' }} />
          <div style={{ fontSize: '2.5rem', fontWeight: 900 }}>{reputation?.reliabilityScore || '0.0'}</div>
          <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', fontWeight: 700 }}>REPUTATION  SCORE</div>
        </div>
        <div className="card" style={{ textAlign: 'center' }}>
          <CheckCircle color="var(--primary)" size={28} style={{ marginBottom: '1rem' }} />
          <div style={{ fontSize: '2.5rem', fontWeight: 900 }}>{reputation?.completedTasks || '0'}</div>
          <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', fontWeight: 700 }}>TASKS COMPLETED</div>
        </div>
        <div className="card" style={{ textAlign: 'center' }}>
          <TrendingUp color="#0284c7" size={28} style={{ marginBottom: '1rem' }} />
          <div style={{ fontSize: '2.5rem', fontWeight: 900 }}>{reputation?.totalReviews || '0'}</div>
          <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', fontWeight: 700 }}>FEEDBACK RECEIVED</div>
        </div>
        <div className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', gap: '1rem', background: 'var(--primary-light)', border: 'none' }}>
          <button onClick={() => setShowOfferModal(true)} className="btn btn-primary" style={{ width: '100%' }}>
            <Plus size={18} /> Post Help Offer
          </button>
          <button onClick={() => setShowRequestModal(true)} className="btn btn-secondary" style={{ width: '100%', borderColor: 'var(--primary)', color: 'var(--primary)' }}>
            <Plus size={18} /> Request Help
          </button>
          <button onClick={() => navigate(`/family/${user.id}`)} className="btn btn-secondary" style={{ width: '100%' }}>
            <User size={18} /> View My Profile
          </button>
        </div>
      </div>

      {/* MY TASKS */}
      {tasks.length > 0 && (
        <div style={{ marginTop: '4rem' }}>
          <h3 style={{ fontWeight: 800, marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <ClipboardCheck size={22} color="var(--primary)" /> My Tasks
          </h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {tasks
              .filter(t => t.helperFamily?.id === user.id || t.requesterFamily?.id === user.id)
              .map(task => (
                <div key={task.id} className="card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1.25rem 1.5rem' }}>
                  <div>
                    <div style={{ fontWeight: 700 }}>{task.helpRequest?.title || task.helpOffer?.title || 'Untitled Task'}</div>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                      {task.status === 'ACTIVE' ? 'In Progress' : 'Completed'}
                      {task.helperFamily && ` · Helped by ${task.helperFamily.familyName}`}
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
                    {task.status === 'ACTIVE' ? (
                      <>
                        <span className="badge badge-info">ACTIVE</span>
                        <button
                          onClick={() => handleCompleteTask(task.id)}
                          className="btn btn-primary"
                          style={{ padding: '0.4rem 1rem', fontSize: '0.8rem' }}
                        >
                          <CheckCircle size={14} /> Mark Complete
                        </button>
                      </>
                    ) : (
                      <span className="badge badge-success">COMPLETED</span>
                    )}
                  </div>
                </div>
              ))
            }
          </div>
        </div>
      )}

      <div className="card" style={{ border: '1px solid #fee2e2', background: 'rgba(239, 68, 68, 0.02)', marginTop: '4rem' }}>
        <h4 style={{ color: '#ef4444', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Trash2 size={18} /> Danger Zone
        </h4>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            Permanently delete your family account and all associated community data.
          </p>
          <button onClick={() => setShowDeleteModal(true)} className="btn btn-secondary" style={{ color: '#ef4444', borderColor: '#ef4444' }}>
            Delete Account
          </button>
        </div>
      </div>

    </section>
  );
};

export default DashboardView;
