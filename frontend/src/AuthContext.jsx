import { createContext, useState, useEffect, useContext } from 'react';
import api from './api';

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if token exists on load
    const token = localStorage.getItem('token');
    const email = localStorage.getItem('email'); // simple persistence
    if (token) {
      // In a real app we'd verify the token with backend here,
      // but for simple prototyping we'll trust local storage if present.
      setUser({ email: email || 'User' });
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    // Assuming backend returns a token. 
    // Wait, the AuthController typically exists.
    try {
      const response = await api.post('/auth/login', { email, password });
      // JWT response from earlier conversation: assumes token in response.
      // Usually standard is response.data
      const token = response.data; // The /auth/login backend returns string token
      localStorage.setItem('token', token);
      localStorage.setItem('email', email);
      setUser({ email });
      return true;
    } catch (err) {
      console.error(err);
      return false;
    }
  };

  const register = async (email, password) => {
    try {
      await api.post('/auth/signup', { email, password });
      return { success: true };
    } catch (err) {
      if (err.response && err.response.data) {
        return { success: false, message: err.response.data };
      }
      return { success: false, message: 'Registration failed' };
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('email');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
