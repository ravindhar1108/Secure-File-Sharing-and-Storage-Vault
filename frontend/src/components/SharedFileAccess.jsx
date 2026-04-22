import { useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../api';
import { FileDown, Lock, ShieldCheck, Eye } from 'lucide-react';

export default function SharedFileAccess() {
  const { token } = useParams();
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [validated, setValidated] = useState(false);
  const [isViewOnce, setIsViewOnce] = useState(false);

  const handleValidate = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const payload = password ? { password } : {};
      const response = await api.post(`/share/${token}/validate`, payload);
      
      if (response.data.valid) {
          setValidated(true);
          setIsViewOnce(response.data.viewOnce);
      }
    } catch (err) {
      if (err.response && err.response.data) {
        const text = err.response.data;
        if (typeof text === 'string') {
            if (text.includes("Password Required") || text.includes("Invalid Password")) {
                setError("Incorrect or missing password.");
            } else if (text.includes("expired") || text.includes("disabled") || text.includes("limit reached")) {
                setError("This secure link has expired or reached its access limit.");
            } else {
                setError(text);
            }
        } else {
            setError("Failed to validate link.");
        }
      } else {
         setError("Network error. Could not reach the server.");
      }
    } finally {
      setLoading(false);
    }
  };

  const executeAccess = (action) => {
    const url = `http://localhost:8080/share/${token}?action=${action}${password ? `&password=${encodeURIComponent(password)}` : ''}`;
    
    // Natively open using browser routing. 
    // This perfectly supports gigantic files (GBs) and native inline previewing!
    if (action === 'view') {
        window.open(url, '_blank');
    } else {
        const link = document.createElement('a');
        link.href = url;
        document.body.appendChild(link);
        link.click();
        link.remove();
    }
  };

  return (
    <div style={{ display: 'flex', height: '100vh', alignItems: 'center', justifyContent: 'center', background: 'var(--surface-color)' }}>
      <div className="animate-fade-in" style={{ padding: '40px', width: '100%', maxWidth: '420px', textAlign: 'center', border: '1px solid var(--border-color)', borderRadius: 'var(--radius)' }}>
        
        <div style={{ 
          height: '48px', width: '48px', 
          margin: '0 auto 16px', color: 'var(--primary-color)', display: 'flex', alignItems: 'center', justifyContent: 'center'
        }}>
          <ShieldCheck size={40} strokeWidth={1.5} />
        </div>
        
        <h1 style={{ fontSize: '24px', fontWeight: '400', marginBottom: '8px', color: 'var(--text-primary)' }}>Secure shared file</h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '15px', marginBottom: '32px' }}>
          {validated 
              ? "Access granted. Choose how you would like to open this file." 
              : "This file is securely hosted on Vault. If a password was provided to you, enter it below."
          }
        </p>

        {error && (
          <div style={{ padding: '12px', background: 'var(--danger-bg)', color: 'var(--danger-color)', borderRadius: 'var(--radius)', fontSize: '14px', marginBottom: '24px' }}>
            {error}
          </div>
        )}

        {!validated ? (
            <form onSubmit={handleValidate} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div>
                <input 
                    type="password" 
                    className="input-field" 
                    placeholder="Enter password (optional for public links)" 
                    style={{ padding: '12px' }}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />
            </div>
            
            <div className="flex justify-end" style={{ marginTop: '16px' }}>
                <button type="submit" className="btn btn-primary" style={{ padding: '10px 24px' }} disabled={loading}>
                    {loading ? 'Verifying...' : 'Next'}
                </button>
            </div>
            </form>
        ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                <button onClick={() => executeAccess('view')} className="btn btn-primary" style={{ padding: '10px 24px' }}>
                    Open file
                </button>
                
                {!isViewOnce ? (
                    <button onClick={() => executeAccess('download')} className="btn btn-outline" style={{ padding: '10px 24px' }}>
                        Download
                    </button>
                ) : (
                    <div style={{ fontSize: '13px', color: 'var(--text-secondary)', marginTop: '8px' }}>
                        The author has restricted this file to View-Only access.
                    </div>
                )}
            </div>
        )}

      </div>
    </div>
  );
}
