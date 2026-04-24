import axios from 'axios';

// Create a configured axios instance
// Note: Hardcodinglocalhost is fine for local dev. In prod it would be an env var.
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
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

// Response interceptor to handle auth errors
api.interceptors.response.use(
  (response) => {
    // If the backend returns HTML (e.g. redirected to login page) when we expect JSON for API calls
    if (response.headers['content-type'] && response.headers['content-type'].includes('text/html')) {
        localStorage.removeItem('token');
        localStorage.removeItem('email');
        window.location.href = '/login';
        return Promise.reject(new Error('Authentication failed (redirected to HTML)'));
    }
    return response;
  },
  (error) => {
    const originalRequest = error.config;
    // Don't intercept auth or share validation requests
    if (originalRequest.url.includes('/auth/') || originalRequest.url.includes('/share/')) {
        return Promise.reject(error);
    }

    if (error.response && (error.response.status === 401)) {
      // Only logout on 401 Unauthorized
      localStorage.removeItem('token');
      localStorage.removeItem('email');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
