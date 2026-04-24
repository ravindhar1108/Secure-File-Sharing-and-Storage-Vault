import { useState, useEffect } from 'react';
import { Share2, Clock, Eye, Download, Trash2, Copy, Check } from 'lucide-react';
import api from '../api';

export default function ActiveLinks() {
  const [links, setLinks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [copiedToken, setCopiedToken] = useState(null);

  useEffect(() => {
    fetchActiveLinks();
  }, []);

  const fetchActiveLinks = async () => {
    try {
      setLoading(true);
      const response = await api.get('/share/active');
      setLinks(response.data);
    } catch (err) {
      setError("Failed to fetch active links.");
    } finally {
      setLoading(false);
    }
  };

  const handleCopyLink = (token) => {
    const url = `${window.location.origin}/share/${token}`;
    navigator.clipboard.writeText(url);
    setCopiedToken(token);
    setTimeout(() => setCopiedToken(null), 2000);
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-full">
        <div className="animate-pulse" style={{ color: 'var(--text-secondary)' }}>Loading your active links...</div>
      </div>
    );
  }

  return (
    <div style={{ padding: '24px', maxWidth: '1000px', margin: '0 auto' }}>
      <div className="flex justify-between items-center" style={{ marginBottom: '32px' }}>
        <div>
          <h1 style={{ fontSize: '28px', fontWeight: '600', color: 'var(--text-primary)', letterSpacing: '-0.5px', marginBottom: '8px' }}>Active Links</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '15px' }}>Manage files you are currently sharing with others.</p>
        </div>
      </div>

      {error && (
        <div style={{ padding: '12px', background: 'var(--danger-bg)', color: 'var(--danger-color)', borderRadius: 'var(--radius)', marginBottom: '24px' }}>
          {error}
        </div>
      )}

      {links.length === 0 ? (
        <div className="surface" style={{ textAlign: 'center', padding: '80px 24px', borderRadius: 'var(--radius-lg)' }}>
          <div style={{ width: '80px', height: '80px', background: 'var(--bg-color)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 20px' }}>
            <Share2 size={40} style={{ color: 'var(--text-tertiary)', opacity: 0.5 }} />
          </div>
          <h3 style={{ fontSize: '18px', fontWeight: '600', marginBottom: '8px', color: 'var(--text-primary)' }}>No active links</h3>
          <p style={{ color: 'var(--text-secondary)' }}>You aren't sharing any files right now. Share a file from your drive to see it here.</p>
        </div>
      ) : (
        <div className="surface" style={{ overflow: 'hidden', borderRadius: 'var(--radius-lg)' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)', fontSize: '12px', textTransform: 'uppercase', letterSpacing: '0.05em', background: '#fafafa' }}>
                <th style={{ padding: '16px 24px', fontWeight: '500' }}>File Name</th>
                <th style={{ padding: '16px 24px', fontWeight: '500' }}>Metrics</th>
                <th style={{ padding: '16px 24px', fontWeight: '500' }}>Expires</th>
                <th style={{ padding: '16px 24px', fontWeight: '500', textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {links.map((link) => (
                <tr key={link.token} className="hover-bg" style={{ borderBottom: '1px solid var(--border-color)', transition: 'background 0.2s', opacity: link.active ? 1 : 0.6 }}>
                  <td style={{ padding: '16px 24px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                      <div style={{ color: 'var(--primary-color)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                        <Share2 size={20} />
                      </div>
                      <div style={{ display: 'flex', flexDirection: 'column' }}>
                        <div style={{ fontWeight: '500', color: 'var(--text-primary)', lineHeight: '1.2', display: 'flex', alignItems: 'center', gap: '8px' }}>
                            {link.fileName || 'Unknown File'}
                            <span style={{ fontSize: '10px', padding: '2px 6px', borderRadius: '10px', background: link.shareType === 'PUBLIC' ? '#dcfce7' : '#fee2e2', color: link.shareType === 'PUBLIC' ? '#166534' : '#991b1b', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                                {link.shareType || 'PRIVATE'}
                            </span>
                        </div>
                        <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '4px' }}>
                          {link.shareType === 'PUBLIC' ? 'Unrestricted Access' : (link.viewOnce ? 'View-Only Access' : 'Secure Download Access')}
                        </div>
                      </div>
                    </div>
                  </td>
                  <td style={{ padding: '16px 24px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '16px', color: 'var(--text-secondary)', fontSize: '13px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }} title="Views">
                        <Eye size={16} /> {link.viewCount}
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }} title="Downloads">
                        <Download size={16} /> {link.downloadCount} {link.maxDownloads && link.maxDownloads < 1000 ? `/ ${link.maxDownloads}` : ''}
                      </div>
                    </div>
                  </td>
                  <td style={{ padding: '16px 24px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <Clock size={16} style={{ color: link.active ? 'var(--text-secondary)' : 'var(--danger-color)' }} />
                      <span style={{ fontSize: '14px', color: link.active ? 'var(--text-primary)' : 'var(--danger-color)' }}>
                        {link.active ? new Date(link.expiryTime).toLocaleString() : 'Inactive/Expired'}
                      </span>
                    </div>
                  </td>
                  <td style={{ padding: '16px 24px', textAlign: 'right' }}>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: '8px' }}>
                      <button 
                        onClick={async () => {
                            try {
                                await api.put(`/share/${link.token}/toggle`);
                                fetchActiveLinks();
                            } catch (err) {
                                setError("Failed to toggle link status.");
                            }
                        }}
                        className="btn btn-outline" 
                        style={{ padding: '6px 12px', fontSize: '13px', borderColor: link.active ? 'var(--border-color)' : 'var(--primary-color)', color: link.active ? 'var(--text-secondary)' : 'var(--primary-color)' }}
                        title={link.active ? "Pause link temporarily" : "Resume link"}
                      >
                        {link.active ? 'Pause' : 'Resume'}
                      </button>
                      <button 
                        onClick={() => handleCopyLink(link.token)}
                        className="btn btn-outline" 
                        style={{ padding: '6px', width: '32px', height: '32px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                        title="Copy Link"
                      >
                        {copiedToken === link.token ? <Check size={16} style={{color: '#137333'}} /> : <Copy size={16} />}
                      </button>
                      <button 
                        onClick={async () => {
                            if(window.confirm("Are you sure you want to permanently delete this shared link?")) {
                                try {
                                    await api.delete(`/share/${link.token}`);
                                    fetchActiveLinks();
                                } catch (err) {
                                    setError("Failed to delete link.");
                                }
                            }
                        }}
                        className="btn btn-outline" 
                        style={{ padding: '6px', width: '32px', height: '32px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--danger-color)', borderColor: 'transparent' }}
                        title="Delete Link"
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
