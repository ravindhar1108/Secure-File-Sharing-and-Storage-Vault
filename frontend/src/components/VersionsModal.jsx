import { useState, useEffect } from 'react';
import api from '../api';
import { X, History, Download } from 'lucide-react';

export default function VersionsModal({ file, onClose }) {
  const [versions, setVersions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchVersions = async () => {
      try {
        const res = await api.get(`/files/${file.id}/versions`);
        setVersions(res.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchVersions();
  }, [file.id]);

  const handleDownloadVersion = async (version) => {
    const token = localStorage.getItem('token');
    const url = `http://localhost:8080/files/version/${version.id}?token=${token}`;
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `v${version.version}_${file.originalName}`);
    document.body.appendChild(link);
    link.click();
    link.remove();
  };

  return (
    <div style={{
      position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
      background: 'rgba(15, 23, 42, 0.5)', backdropFilter: 'blur(4px)',
      display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 50
    }}>
      <div className="surface animate-fade-in" style={{ width: '100%', maxWidth: '500px', padding: '32px', maxHeight: '80vh', overflowY: 'auto' }}>
        
        <div className="flex justify-between items-center" style={{ marginBottom: '24px' }}>
          <h2 style={{ fontSize: '18px', fontWeight: '600', display: 'flex', alignItems: 'center', gap: '8px' }}>
             <History size={18} style={{ color: 'var(--text-secondary)'}} />
             Version History
          </h2>
          <button onClick={onClose} style={{ color: 'var(--text-tertiary)' }}><X size={20}/></button>
        </div>

        <p style={{ color: 'var(--text-secondary)', fontSize: '14px', marginBottom: '16px' }}>
            Historical overwrites for "{file.originalName}"
        </p>

        {loading ? (
           <div style={{ color: 'var(--text-tertiary)', fontSize: '14px' }}>Loading versions...</div>
        ) : versions.length === 0 ? (
           <div style={{ padding: '24px', textAlign: 'center', background: '#f8fafc', borderRadius: 'var(--radius)', border: '1px dashed var(--border-color)', color: 'var(--text-secondary)', fontSize: '14px' }}>
               No older versions exist for this file.
           </div>
        ) : (
           <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {versions.map(v => (
                  <div key={v.id} className="flex justify-between items-center surface" style={{ padding: '16px', background: '#f8fafc', boxShadow: 'none' }}>
                      <div>
                          <div style={{ fontWeight: '500', fontSize: '14px' }}>Version {v.version}</div>
                          <div style={{ color: 'var(--text-tertiary)', fontSize: '12px', marginTop: '4px' }}>
                              {(v.size / 1024).toFixed(2)} KB • {new Date(v.uploadedAt).toLocaleString()}
                          </div>
                      </div>
                      <button onClick={() => handleDownloadVersion(v)} className="btn btn-outline" style={{ padding: '6px 12px', fontSize: '13px' }}>
                          <Download size={14} /> Download
                      </button>
                  </div>
              ))}
           </div>
        )}

      </div>
    </div>
  );
}
