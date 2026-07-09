import { useEffect } from 'react';
import { Routes, Route, useNavigate } from 'react-router-dom';
import DailyPage from './pages/DailyPage';
import PastGamePage from './pages/PastGamePage';
import { SITE_NAME } from './features/seo/meta';

export function AppRoutes() {
  useEffect(() => {
    document.title = SITE_NAME;
  }, []);

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
