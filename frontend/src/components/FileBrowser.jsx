import { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api';
import { Folder, File as FileIcon, UploadCloud, FolderPlus, Download, Trash2, ArrowLeft, History, Share2, ExternalLink, FolderOpen } from 'lucide-react';
import ShareModal from './ShareModal';
import VersionsModal from './VersionsModal';

export default function FileBrowser() {
  const { folderId } = useParams();
  const navigate = useNavigate();
  
  const [folders, setFolders] = useState([]);
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isDragging, setIsDragging] = useState(false);
  
  const [selectedFileForShare, setSelectedFileForShare] = useState(null);
  const [selectedFileForVersions, setSelectedFileForVersions] = useState(null);
  
  const fileInputRef = useRef(null);

  const fetchContents = useCallback(async () => {
    setLoading(true);
    try {
      const [foldersRes, filesRes] = await Promise.all([
        folderId ? api.get(`/folders/${folderId}/subfolders`) : api.get('/folders'),
        api.get(`/files${folderId ? `?folderId=${folderId}` : ''}`)
      ]);
      setFolders(Array.isArray(foldersRes.data) ? foldersRes.data : []);
      setFiles(Array.isArray(filesRes.data) ? filesRes.data : []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [folderId]);

  useEffect(() => {
    fetchContents();
  }, [fetchContents]);

  const handleCreateFolder = async () => {
    const name = window.prompt("Enter folder name:");
    if (!name) return;
    try {
      await api.post(`/folders?name=${encodeURIComponent(name)}${folderId ? `&parentId=${folderId}` : ''}`);
      fetchContents();
    } catch (err) {
      alert("Error creating folder.");
    }
  };

  const handleFileUpload = async (fileObj) => {
    const formData = new FormData();
    formData.append('file', fileObj);
    
    try {
      await api.post(`/files/upload${folderId ? `?folderId=${folderId}` : ''}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      fetchContents();
    } catch (err) {
      alert("Error uploading file.");
    }
  };

  const onDrop = (e) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      Array.from(e.dataTransfer.files).forEach(file => handleFileUpload(file));
      e.dataTransfer.clearData();
    }
  };

  const handleDownload = async (file) => {
    const token = localStorage.getItem('token');
    const url = `http://localhost:8080/files/${file.id}?token=${token}`;
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', file.originalName);
    document.body.appendChild(link);
    link.click();
    link.remove();
  };

  const handleOpenPreview = async (file) => {
    const token = localStorage.getItem('token');
    const url = `http://localhost:8080/files/${file.id}?token=${token}&action=view`;
    window.open(url, '_blank');
  };

  const handleDeleteFile = async (id) => {
    try {
      await api.delete(`/files/${id}`);
      fetchContents();
    } catch(err) {
      alert("Failed to delete file");
    }
  };

  const handleDeleteFolder = async (id) => {
     if(window.confirm("Delete folder and move its contents to trash?")) {
        try {
          await api.delete(`/folders/${id}`);
          fetchContents();
        } catch(err) {
          alert("Failed to delete folder");
        }
     }
  };

  return (
    <div style={{ paddingBottom: '40px' }}
         onDragOver={(e) => { e.preventDefault(); setIsDragging(true); }}
         onDragLeave={(e) => { e.preventDefault(); setIsDragging(false); }}
         onDrop={onDrop}>
      
      <div className="flex justify-between items-center" style={{ marginBottom: '32px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            {folderId && (
            <button onClick={() => navigate(-1)} className="btn" style={{ padding: '8px', background: 'var(--surface-color)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)', boxShadow: 'var(--shadow-sm)' }}>
                <ArrowLeft size={20} style={{ color: 'var(--text-secondary)' }} />
            </button>
            )}
            <h1 style={{ fontSize: '28px', fontWeight: '600', color: 'var(--text-primary)', letterSpacing: '-0.5px' }}>{folderId ? 'Folder Contents' : 'My Drive'}</h1>
        </div>
        
        <div className="flex gap-4">
          <button onClick={handleCreateFolder} className="btn btn-outline" style={{ borderRadius: 'var(--radius-sm)' }}>
            <FolderPlus size={18} /> New folder
          </button>
          <button onClick={() => fileInputRef.current.click()} className="btn btn-primary" style={{ borderRadius: 'var(--radius-sm)' }}>
            <UploadCloud size={18} /> Upload files
          </button>
          <input 
            type="file" 
            multiple 
            ref={fileInputRef} 
            onChange={(e) => Array.from(e.target.files).forEach(f => handleFileUpload(f))} 
            style={{ display: 'none' }} 
          />
        </div>
      </div>

      {isDragging && (
        <div className="surface animate-fade-in" style={{ 
            height: '140px', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
            border: '2px dashed var(--accent-color)', background: 'var(--accent-bg)', marginBottom: '32px', color: 'var(--accent-color)', fontWeight: '500', borderRadius: 'var(--radius-lg)' }}>
            <UploadCloud size={40} style={{ marginBottom: '12px', opacity: 0.8 }}/>
            Drop your files here to upload to this directory
        </div>
      )}

      {loading ? (
        <div>Loading...</div>
      ) : (
        <div className="surface" style={{ overflow: 'hidden', borderRadius: 'var(--radius-lg)' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '14px' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)', background: '#fafafa' }}>
                <th style={{ padding: '16px 24px', fontWeight: '500' }}>Name</th>
                <th style={{ padding: '16px 24px', fontWeight: '500' }}>Size</th>
                <th style={{ padding: '16px 24px', fontWeight: '500' }}>Last modified</th>
                <th style={{ padding: '16px 24px', fontWeight: '500', textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {folders.map(folder => (
                <tr key={`folder-${folder.id}`} style={{ borderBottom: '1px solid var(--border-color)', transition: 'background 0.2s' }} className="hover-row">
                  <td style={{ padding: '16px 24px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px', fontWeight: '500', cursor: 'pointer' }} onClick={() => navigate(`/folder/${folder.id}`)}>
                      <Folder size={20} style={{ color: 'var(--accent-color)' }} fill="currentColor" fillOpacity={0.2} />
                      {folder.name}
                    </div>
                  </td>
                  <td style={{ padding: '16px 24px', color: 'var(--text-tertiary)' }}>--</td>
                  <td style={{ padding: '16px 24px', color: 'var(--text-tertiary)' }}>{new Date(folder.createdAt).toLocaleDateString()}</td>
                  <td style={{ padding: '16px 24px', textAlign: 'right' }}>
                    <button onClick={() => handleDeleteFolder(folder.id)} style={{ color: 'var(--text-tertiary)' }}><Trash2 size={16} /></button>
                  </td>
                </tr>
              ))}

              {files.map(file => (
                <tr key={`file-${file.id}`} style={{ borderBottom: '1px solid var(--border-color)', transition: 'background 0.2s' }} className="hover-row">
                  <td style={{ padding: '16px 24px', cursor: 'pointer' }} onClick={() => handleOpenPreview(file)}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px', fontWeight: '500' }}>
                      <FileIcon size={20} style={{ color: 'var(--accent-color)' }} />
                      <div className="hover-underline">
                        {file.originalName}
                        {file.version > 1 && <span style={{ marginLeft: '8px', fontSize: '11px', background: '#e2e8f0', padding: '2px 6px', borderRadius: '4px', color: 'var(--text-secondary)', textDecoration: 'none', display: 'inline-block' }}>v{file.version}</span>}
                      </div>
                    </div>
                  </td>
                  <td style={{ padding: '16px 24px', color: 'var(--text-secondary)' }}>{(file.size / 1024).toFixed(2)} KB</td>
                  <td style={{ padding: '16px 24px', color: 'var(--text-secondary)' }}>{new Date(file.uploadedAt).toLocaleDateString()}</td>
                  <td style={{ padding: '16px 24px', textAlign: 'right' }}>
                    <div className="flex gap-2 justify-center" style={{ justifyContent: 'flex-end' }}>
                       <button onClick={() => setSelectedFileForShare(file)} title="Generate Secure Link" style={{ color: 'var(--text-secondary)', padding: '4px', background: 'none' }}><Share2 size={16} /></button>
                       <button onClick={() => setSelectedFileForVersions(file)} title="View Versions" style={{ color: 'var(--text-secondary)', padding: '4px', background: 'none' }}><History size={16} /></button>
                       <button onClick={() => handleDownload(file)} title="Download" style={{ color: 'var(--accent-color)', padding: '4px', background: 'none' }}><Download size={16} /></button>
                       <button onClick={() => handleDeleteFile(file.id)} title="Send to Trash" style={{ color: 'var(--danger-color)', padding: '4px', background: 'none' }}><Trash2 size={16} /></button>
                    </div>
                  </td>
                </tr>
              ))}

              {folders.length === 0 && files.length === 0 && (
                <tr>
                   <td colSpan={4} style={{ padding: '64px', textAlign: 'center', color: 'var(--text-tertiary)' }}>
                       <div style={{ width: '80px', height: '80px', background: 'var(--bg-color)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 20px' }}>
                         <FolderOpen size={40} style={{ color: 'var(--text-tertiary)', opacity: 0.5 }} />
                       </div>
                       <p style={{ fontSize: '16px', fontWeight: '500', color: 'var(--text-secondary)' }}>This folder is empty</p>
                       <p style={{ marginTop: '8px', fontSize: '14px' }}>Upload files or create a new folder to get started.</p>
                   </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
      
      {selectedFileForShare && <ShareModal file={selectedFileForShare} onClose={() => setSelectedFileForShare(null)} />}
      {selectedFileForVersions && <VersionsModal file={selectedFileForVersions} onClose={() => setSelectedFileForVersions(null)} />}

      <style>{`
        .hover-row:hover { background: #f8fafc; }
        .hover-underline:hover { text-decoration: underline; color: var(--accent-color); }
      `}</style>
    </div>
  );
}
