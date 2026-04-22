import { useState } from 'react';
import { useAuth } from '../AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';
import { KeyRound, Mail } from 'lucide-react';

export default function Login({ mode = 'login' }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const isRegistering = mode === 'signup';
  const location = useLocation();
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState(location.state?.successMessage || '');
  const [loading, setLoading] = useState(false);
  const { login, register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMsg('');
    
    if (isRegistering && password !== confirmPassword) {
        setError('Passwords do not match');
        return;
    }

    setLoading(true);
    
    try {
      if (isRegistering) {
        const result = await register(email, password);
        if (result.success) {
           setError('');
           navigate('/login', { state: { successMessage: 'Account created successfully! Please sign in.' } });
           setEmail('');
           setPassword('');
           setConfirmPassword('');
        } else {
           setError(result.message);
        }
      } else {
        const success = await login(email, password);
        if (success) {
          navigate('/');
        } else {
          setError('Invalid credentials.');
        }
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', height: '100vh', alignItems: 'center', justifyContent: 'center', background: 'var(--surface-color)' }}>
      <div className="animate-fade-in" style={{ padding: '48px 40px 36px', width: '100%', maxWidth: '448px', border: '1px solid #dadce0', borderRadius: '8px' }}>
        
        <div key={isRegistering ? 'register' : 'login'} className="slide-enter">
          <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <div style={{ 
            height: '48px', width: '48px',
            margin: '0 auto 16px', color: 'var(--primary-color)', display: 'flex', alignItems: 'center', justifyContent: 'center'
          }}>
            <KeyRound size={40} strokeWidth={1.5} />
          </div>
          <h1 style={{ fontSize: '24px', fontWeight: '400', color: 'var(--text-primary)' }}>Vault</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '16px', marginTop: '8px' }}>
            {isRegistering ? 'Create your account' : 'Sign in'}
          </p>
        </div>

        {error && (
          <div style={{ padding: '12px', background: 'var(--danger-bg)', color: 'var(--danger-color)', borderRadius: 'var(--radius)', fontSize: '14px', marginBottom: '16px', textAlign: 'center' }}>
            {error}
          </div>
        )}

        {successMsg && !isRegistering && (
          <div className="animate-fade-in" style={{ padding: '12px', background: '#e6f4ea', color: '#137333', borderRadius: 'var(--radius)', fontSize: '14px', marginBottom: '16px', textAlign: 'center' }}>
            {successMsg}
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div>
            <div style={{ position: 'relative' }}>
              <Mail size={20} style={{ position: 'absolute', left: '12px', top: '11px', color: 'var(--text-tertiary)' }} strokeWidth={1.5}/>
              <input 
                type="email" 
                className="input-field" 
                placeholder="Email or phone" 
                style={{ paddingLeft: '40px', padding: '12px 12px 12px 40px' }}
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
          </div>
          <div>
            <input 
                type="password" 
                className="input-field" 
                placeholder="Enter your password" 
                style={{ padding: '12px' }}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
            />
          </div>
          {isRegistering && (
          <div className="slide-enter">
            <input 
                type="password" 
                className="input-field" 
                placeholder="Confirm your password" 
                style={{ padding: '12px' }}
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
            />
          </div>
          )}
          <div className="flex justify-between items-center" style={{ marginTop: '32px' }}>
            <button type="button" onClick={() => navigate(isRegistering ? '/login' : '/signup')} style={{ color: 'var(--primary-color)', fontWeight: '500', fontSize: '14px', background: 'none' }}>
              {isRegistering ? 'Sign in instead' : 'Create account'}
            </button>
            <button type="submit" className="btn btn-primary" style={{ padding: '10px 24px', minWidth: '88px', display: 'flex', justifyContent: 'center' }} disabled={loading}>
              {loading ? <span className="spinner"></span> : 'Next'}
            </button>
          </div>
        </form>
        </div>
      </div>
    </div>
  );
}
