import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { AdminRoute } from './components/auth/AdminRoute';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { Layout } from './components/layout/Layout';
import { AuthProvider } from './context/AuthContext';
import { AiRefinePage } from './pages/AiRefinePage';
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/LoginPage';
import { NewsCreatePage } from './pages/NewsCreatePage';
import { NewsDetailPage } from './pages/NewsDetailPage';
import { NewsPage } from './pages/NewsPage';
import { OAuth2CallbackPage } from './pages/OAuth2CallbackPage';
import { PromptCreatePage } from './pages/PromptCreatePage';
import { PromptDetailPage } from './pages/PromptDetailPage';
import { PromptEditPage } from './pages/PromptEditPage';

export function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/prompts/:id" element={<PromptDetailPage />} />
            <Route path="/news" element={<NewsPage />} />
            <Route path="/news/:id" element={<NewsDetailPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/oauth2/callback" element={<OAuth2CallbackPage />} />

            <Route element={<ProtectedRoute />}>
              <Route path="/prompts/new" element={<PromptCreatePage />} />
              <Route path="/prompts/:id/edit" element={<PromptEditPage />} />
              <Route path="/ai/refine" element={<AiRefinePage />} />
            </Route>

            <Route element={<AdminRoute />}>
              <Route path="/news/new" element={<NewsCreatePage />} />
            </Route>
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
