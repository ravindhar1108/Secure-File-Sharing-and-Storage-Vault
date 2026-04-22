import { useState } from 'react';
import api from '../api';
import { X, Link as LinkIcon, ShieldAlert } from 'lucide-react';

export default function ShareModal({ file, onClose }) {
  const [loading, setLoading] = useState(false);
  const [link, setLink] = useState('');
  
  const [password, setPassword] = useState('');
  const [expiryHours, setExpiryHours] = useState(24);
  const [maxDownloads, setMaxDownloads] = useState('');
  const [viewOnce, setViewOnce] = useState(false);

  const handleGenerate = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const payload = {
         password: password || null,
         expiryHours: parseInt(expiryHours) || 24,
         maxDownloads: maxDownloads ? parseInt(maxDownloads) : null,
         viewOnce: viewOnce
      };
      
      const res = await api.post(`/share/create/${file.id}`, payload);
      // The backend returns a string 'http://localhost:8080/share/uuid'
      const rawUrl = res.data;
      const token = rawUrl.substring(rawUrl.lastIndexOf('/') + 1);
      const frontendLink = `${window.location.origin}/share/${token}`;
      setLink(frontendLink);
    } catch (err) {
      alert('Failed to generate secure link');
    } finally {
      setLoading(false);
    }
  };

  const copyToClipboard = () => {
    navigator.clipboard.writeText(link);
    alert('Link copied to clipboard!');
  };

  return (
    <div style={{
      position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
      background: 'rgba(15, 23, 42, 0.5)', backdropFilter: 'blur(4px)',
      display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 50
    }}>
      <div className="surface animate-fade-in" style={{ width: '100%', maxWidth: '440px', padding: '32px' }}>
        
        <div className="flex justify-between items-center" style={{ marginBottom: '24px' }}>
          <h2 style={{ fontSize: '18px', fontWeight: '600' }}>Share "{file.originalName}"</h2>
          <button onClick={onClose} style={{ color: 'var(--text-tertiary)' }}><X size={20}/></button>
        </div>

        {!link ? (
          <form onSubmit={handleGenerate} className="flex" style={{ flexDirection: 'column', gap: '16px' }}>
            <div>
               <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', marginBottom: '6px', color: 'var(--text-secondary)' }}>Password Protection (Optional)</label>
               <input type="password" placeholder="Leave blank for public link" className="input-field" value={password} onChange={(e) => setPassword(e.target.value)} />
            </div>
            
            <div className="flex gap-4">
                <div style={{ flex: 1 }}>
                   <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', marginBottom: '6px', color: 'var(--text-secondary)' }}>Expiry (Hours)</label>
                   <input type="number" min="1" className="input-field" value={expiryHours} onChange={(e) => setExpiryHours(e.target.value)} required />
                </div>
                <div style={{ flex: 1 }}>
                   <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', marginBottom: '6px', color: 'var(--text-secondary)' }}>Max Downloads</label>
                   <input type="number" min="1" placeholder="Unlimited" className="input-field" value={maxDownloads} onChange={(e) => setMaxDownloads(e.target.value)} />
                </div>
            </div>

            <label className="flex items-center gap-2" style={{ fontSize: '14px', color: 'var(--text-primary)', cursor: 'pointer', marginTop: '8px' }}>
                <input type="checkbox" checked={viewOnce} onChange={(e) => setViewOnce(e.target.checked)} style={{ width: '16px', height: '16px' }}/>
                View Once (Restricts download and self-destructs after being viewed once)
            </label>

            <button type="submit" className="btn btn-primary" style={{ marginTop: '16px' }} disabled={loading}>
              <LinkIcon size={16} /> {loading ? 'Generating...' : 'Generate Secure Link'}
            </button>
          </form>
        ) : (
          <div style={{ textAlign: 'center' }}>
             <div style={{ padding: '16px', background: 'rgba(59, 130, 246, 0.1)', color: 'var(--accent-color)', borderRadius: 'var(--radius)', fontSize: '14px', wordBreak: 'break-all', marginBottom: '16px' }}>
                {link}
             </div>
             
             {password && (
                 <div className="flex items-center justify-center gap-2" style={{ color: 'var(--text-secondary)', fontSize: '13px', marginBottom: '24px' }}>
                     <ShieldAlert size={16} style={{ color: 'var(--danger-color)' }} />
                     Remember to share your password securely.
                 </div>
             )}

             <button onClick={copyToClipboard} className="btn btn-primary" style={{ width: '100%' }}>Copy Link</button>
          </div>
        )}

      </div>
    </div>
  );
}
