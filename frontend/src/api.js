import axios from 'axios';

// Create a configured axios instance
// Note: Hardcodinglocalhost is fine for local dev. In prod it would be an env var.
const api = axios.create({
  baseURL: 'http://localhost:8080',
});

// Request interceptor to attach JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default api;
