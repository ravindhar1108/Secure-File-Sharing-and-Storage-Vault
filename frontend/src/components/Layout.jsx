import { Outlet, Link, useLocation } from 'react-router-dom';
import { useAuth } from '../AuthContext';
import { HardDrive, Trash2, FolderOpen, LogOut, Settings, Link2 } from 'lucide-react';

export default function Layout() {
  const { user, logout } = useAuth();
  const location = useLocation();

  const NavItem = ({ to, icon: Icon, label }) => {
    const isActive = location.pathname === to || location.pathname.startsWith(`${to}/`);
    return (
      <Link to={to} style={{
        display: 'flex', alignItems: 'center', gap: '12px', padding: '10px 16px',
        borderRadius: 'var(--radius-sm)', 
        color: isActive ? 'var(--accent-color)' : 'var(--text-secondary)',
        background: isActive ? 'var(--accent-bg)' : 'transparent',
        fontWeight: '500',
        transition: 'all 0.2s ease',
        textDecoration: 'none',
        margin: '0 16px'
      }}>
        <Icon size={20} style={{ color: isActive ? 'var(--accent-color)' : 'var(--text-tertiary)' }} />
        {label}
      </Link>
    );
  };

  return (
    <div style={{ display: 'flex', height: '100vh', background: 'var(--bg-color)' }}>
      {/* Sidebar */}
      <aside style={{ width: '260px', background: 'var(--surface-color)', borderRight: '1px solid var(--border-color)', display: 'flex', flexDirection: 'column', zIndex: 10 }}>
        <div style={{ padding: '24px 32px', display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{ height: '40px', width: '40px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--primary-color)', background: 'var(--bg-color)', borderRadius: '10px', border: '1px solid var(--border-color)' }}>
            <HardDrive size={24} />
          </div>
          <span style={{ fontWeight: '600', fontSize: '20px', color: 'var(--text-primary)', letterSpacing: '-0.5px' }}>Vault</span>
        </div>

        <nav style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '2px', marginTop: '8px' }}>
          <NavItem to="/" icon={FolderOpen} label="My Files" />
          <NavItem to="/active-links" icon={Link2} label="Active Links" />
          <NavItem to="/trash" icon={Trash2} label="Trash Bin" />
        </nav>

        <div style={{ padding: '24px', borderTop: '1px solid var(--border-color)', marginTop: 'auto' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '16px' }}>
            <div style={{ height: '36px', width: '36px', borderRadius: '50%', background: 'var(--primary-color)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: '500', color: 'white', flexShrink: 0 }}>
              {user?.email?.[0].toUpperCase()}
            </div>
            <div style={{ fontSize: '14px', fontWeight: '500', textOverflow: 'ellipsis', overflow: 'hidden', color: 'var(--text-primary)' }}>{user?.email}</div>
          </div>
          <button onClick={logout} className="btn btn-outline" style={{ width: '100%', justifyContent: 'center', color: 'var(--text-secondary)' }}>
            <LogOut size={16} /> Sign out
          </button>
        </div>
      </aside>

      <main style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        
        {/* Main Content Area */}
        <div className="animate-fade-in" style={{ flex: 1, overflowY: 'auto', padding: '40px 48px' }}>
          <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
            <Outlet />
          </div>
        </div>
      </main>
    </div>
  );
}
