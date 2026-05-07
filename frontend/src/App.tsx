import { useEffect } from 'react';
import { Routes, Route, useNavigate } from 'react-router-dom';
import DailyPage from './pages/DailyPage';
import PastGamePage from './pages/PastGamePage';

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<DailyPage />} />
      <Route path="/previous_game/:date" element={<PastGamePage />} />
      <Route path="*" element={<RedirectToHome />} />
    </Routes>
  );
}

function RedirectToHome() {
  const navigate = useNavigate();
  useEffect(() => { navigate('/'); }, [navigate]);
  return null;
}
