import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import Login from './components/Login';
import Layout from './components/Layout';
import FileBrowser from './components/FileBrowser';
import TrashBin from './components/TrashBin';
import SharedFileAccess from './components/SharedFileAccess';

const ProtectedRoute = ({ children }) => {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return children;
};

function App() {
  const { loading } = useAuth();

  if (loading) return <div style={{ padding: 20 }}>Loading...</div>;

  return (
    <Routes>
      <Route path="/login" element={<Login mode="login" />} />
      <Route path="/signup" element={<Login mode="signup" />} />
      <Route path="/share/:token" element={<SharedFileAccess />} />
      <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
        <Route index element={<FileBrowser />} />
        <Route path="folder/:folderId" element={<FileBrowser />} />
        <Route path="trash" element={<TrashBin />} />
      </Route>
    </Routes>
  );
}

export default App;
