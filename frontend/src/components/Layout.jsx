import { Outlet, Link, useLocation } from 'react-router-dom';
import { useAuth } from '../AuthContext';
import { HardDrive, Trash2, FolderOpen, LogOut, Settings } from 'lucide-react';

export default function Layout() {
  const { user, logout } = useAuth();
  const location = useLocation();

  const NavItem = ({ to, icon: Icon, label }) => {
    const isActive = location.pathname === to || location.pathname.startsWith(`${to}/`);
    return (
      <Link to={to} style={{
        display: 'flex', alignItems: 'center', gap: '16px', padding: '10px 24px',
        borderRadius: '0 24px 24px 0', 
        color: isActive ? 'var(--primary-color)' : 'var(--text-primary)',
        background: isActive ? '#e8f0fe' : 'transparent',
        fontWeight: isActive ? '500' : '400',
        transition: 'all 0.1s ease',
        textDecoration: 'none',
        marginRight: '16px'
      }}>
        <Icon size={20} style={{ color: isActive ? 'var(--primary-color)' : 'var(--text-secondary)' }} />
        {label}
      </Link>
    );
  };

  return (
    <div style={{ display: 'flex', height: '100vh', background: 'var(--bg-color)' }}>
      {/* Sidebar */}
      <aside style={{ width: '256px', background: 'var(--bg-color)', display: 'flex', flexDirection: 'column' }}>
        <div style={{ padding: '24px', display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{ height: '40px', width: '40px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--primary-color)' }}>
            <HardDrive size={32} />
          </div>
          <span style={{ fontWeight: '500', fontSize: '22px', color: 'var(--text-secondary)' }}>Drive</span>
        </div>

        <nav style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '2px', marginTop: '8px' }}>
          <NavItem to="/" icon={FolderOpen} label="My Files" />
          <NavItem to="/trash" icon={Trash2} label="Trash Bin" />
        </nav>

        <div style={{ padding: '20px 24px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
            <div style={{ height: '32px', width: '32px', borderRadius: '50%', background: 'var(--primary-color)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: '500', color: 'white'}}>
              {user?.email?.[0].toUpperCase()}
            </div>
            <div style={{ fontSize: '14px', fontWeight: '500', textOverflow: 'ellipsis', overflow: 'hidden' }}>{user?.email}</div>
          </div>
          <button onClick={logout} className="btn" style={{ width: '100%', justifyContent: 'flex-start', color: 'var(--text-tertiary)', padding: '8px 0' }}>
            <LogOut size={16} /> Sign out
          </button>
        </div>
      </aside>

      <main style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden', padding: '16px 16px 16px 0' }}>
        
        {/* Main Content Area */}
        <div className="surface" style={{ flex: 1, overflowY: 'auto', padding: '32px', borderRadius: '16px' }}>
          <Outlet />
        </div>
      </main>
    </div>
  );
}
