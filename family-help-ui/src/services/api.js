import axios from 'axios';

const API_BASE_URL = 'https://localhost:8443/api';

const api = axios.create({
  baseURL: API_BASE_URL,
});

// Interceptor to add JWT token to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const authService = {
  login: (email, password) => api.post('/auth/signin', { email, password }),
  signup: (userData) => api.post('/auth/signup', userData),
};

export const categoryService = {
  getAll: () => api.get('/categories'),
  create: (name) => api.post('/categories', { name }),
};

export const helpService = {
  getOffers: () => api.get('/offers'),
  getRequests: () => api.get('/requests'),
  createOffer: (offer) => api.post('/offers', offer),
  createRequest: (request) => api.post('/requests', request),
};

export const taskService = {
  getTasks: (params) => api.get('/tasks', { params }),
  createTask: (task) => api.post('/tasks', task),
  completeTask: (taskId) => api.put(`/tasks/${taskId}`, { status: 'COMPLETED' }),
};

export const reputationService = {
  getReputation: (familyId) => api.get(`/reputations/${familyId}`),
};

export const feedbackService = {
  // Get all feedback for a target family (to display reviews)
  getByTarget: (targetFamilyId) => api.get('/feedback', { params: { targetFamilyId } }),
  // Check if current user already rated a specific task
  getByTask: (helpTaskId) => api.get('/feedback', { params: { helpTaskId } }),
  // Submit a new rating
  submit: (payload) => api.post('/feedback', payload),
};

export default api;
