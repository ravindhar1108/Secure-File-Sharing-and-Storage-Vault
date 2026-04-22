import { useState, useEffect } from 'react';
import api from '../api';
import { File as FileIcon, RefreshCw, XCircle, Trash2 } from 'lucide-react';

export default function TrashBin() {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchTrash = async () => {
    setLoading(true);
    try {
      const res = await api.get('/files/trash');
      setFiles(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTrash();
  }, []);

  const handleRestore = async (id) => {
    try {
      await api.post(`/files/${id}/restore`);
      fetchTrash();
    } catch (err) {
      alert("Failed to restore file.");
    }
  };

  const handlePermanentDelete = async (id) => {
    if (window.confirm("This will permanently delete the file and all its versions. Are you sure?")) {
      try {
        await api.delete(`/files/${id}/permanent`);
        fetchTrash();
      } catch (err) {
        alert("Failed to permanently delete file.");
      }
    }
  };

  return (
    <div>
      <div style={{ marginBottom: '24px' }}>
        <h1 style={{ fontSize: '24px', fontWeight: '600', color: 'var(--text-primary)' }}>Trash Bin</h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '14px', marginTop: '4px' }}>
            Files here will be permanently deleted after 30 days.
        </p>
      </div>

      {loading ? (
        <div>Loading...</div>
      ) : (
        <div className="surface" style={{ overflow: 'hidden' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '14px' }}>
            <thead>
              <tr style={{ background: '#f8fafc', borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)' }}>
                <th style={{ padding: '16px 24px', fontWeight: '500' }}>Name</th>
                <th style={{ padding: '16px 24px', fontWeight: '500' }}>Size</th>
                <th style={{ padding: '16px 24px', fontWeight: '500' }}>Deleted On</th>
                <th style={{ padding: '16px 24px', fontWeight: '500', textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {files.map(file => (
                <tr key={`trash-${file.id}`} style={{ borderBottom: '1px solid var(--border-color)' }}>
                  <td style={{ padding: '16px 24px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px', fontWeight: '500', color: 'var(--text-secondary)' }}>
                      <FileIcon size={20} />
                      <span style={{ textDecoration: 'line-through' }}>{file.originalName}</span>
                    </div>
                  </td>
                  <td style={{ padding: '16px 24px', color: 'var(--text-secondary)' }}>{(file.size / 1024).toFixed(2)} KB</td>
                  <td style={{ padding: '16px 24px', color: 'var(--text-secondary)' }}>{new Date(file.deletedAt).toLocaleDateString()}</td>
                  <td style={{ padding: '16px 24px', textAlign: 'right' }}>
                    <div className="flex gap-2 justify-center" style={{ justifyContent: 'flex-end' }}>
                       <button onClick={() => handleRestore(file.id)} className="btn btn-outline" style={{ padding: '4px 12px', fontSize: '13px' }}>
                           <RefreshCw size={14} /> Restore
                       </button>
                       <button onClick={() => handlePermanentDelete(file.id)} className="btn btn-danger" style={{ padding: '4px 12px', fontSize: '13px' }}>
                           <XCircle size={14} /> Delete Forever
                       </button>
                    </div>
                  </td>
                </tr>
              ))}
              
              {files.length === 0 && (
                <tr>
                   <td colSpan={4} style={{ padding: '48px', textAlign: 'center', color: 'var(--text-tertiary)' }}>
                       <Trash2 size={48} style={{ opacity: 0.2, margin: '0 auto 12px' }} />
                       <p>Trash is empty.</p>
                   </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
