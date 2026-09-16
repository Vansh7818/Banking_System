import axios from 'axios';

const api = axios.create({
  baseURL: `${window.location.protocol}//${window.location.hostname}:8080/api`,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;

export const authApi = {
  login: (email: string, password: string) =>
    api.post('/auth/login', { email, password }),
  ping: () => api.get('/auth/ping'),
  register: (data: any) => api.post('/auth/register', data),
};

export const userApi = {
  getAll: () => api.get('/users'),
  getById: (id: string) => api.get(`/users/${id}`),
};

export const makerCheckerApi = {
  getPending: () => api.get('/maker-checker/pending'),
  approve: (id: string) => api.post(`/maker-checker/${id}/approve`),
  reject: (id: string, rejectionReason: string) =>
    api.post(`/maker-checker/${id}/reject`, { rejectionReason }),
};

export const transactionApi = {
  getAll: () => api.get('/transactions'),
};

export const paymentApi = {
  initiate: (type: string, payload: Record<string, unknown>) =>
    api.post(`/payments/${type.toLowerCase()}`, payload),
};

export const llmApi = {
  analyze: (text: string) => api.post('/llm/analyze', { transactionDetails: text }),
};
