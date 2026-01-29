import React, { Suspense, lazy } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Box, CircularProgress } from '@mui/material';
import { Layout } from './components/Layout/Layout';
import { ProtectedRoute } from './components/Auth/ProtectedRoute';
import { useAuthStore } from './store/authStore';

// Lazy loading stron
const HomePage = lazy(() => import('./pages/HomePage'));
const CalculatorPage = lazy(() => import('./pages/CalculatorPage'));
const RecipesPage = lazy(() => import('./pages/RecipesPage'));
const RecipeDetailPage = lazy(() => import('./pages/RecipeDetailPage'));
const ProfilePage = lazy(() => import('./pages/ProfilePage'));
const LoginPage = lazy(() => import('./pages/LoginPage'));
const RegisterPage = lazy(() => import('./pages/RegisterPage'));
const StylesGuidePage = lazy(() => import('./pages/StylesGuidePage'));
const KnowledgeBasePage = lazy(() => import('./pages/KnowledgeBasePage'));
const ActivePizzaPage = lazy(() => import('./pages/ActivePizzaPage'));

// Loading component
const PageLoader = () => (
  <Box
    sx={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      minHeight: '60vh',
    }}
  >
    <CircularProgress size={60} thickness={4} />
  </Box>
);

function App() {
  const { isAuthenticated } = useAuthStore();

  return (
    <Layout>
      <Suspense fallback={<PageLoader />}>
        <Routes>
          {/* Publiczne strony */}
          <Route path="/" element={<HomePage />} />
          <Route path="/calculator" element={<CalculatorPage />} />
          <Route path="/styles" element={<StylesGuidePage />} />
          <Route path="/knowledge" element={<KnowledgeBasePage />} />
          <Route path="/knowledge/:slug" element={<KnowledgeBasePage />} />
          
          {/* Strony autentykacji */}
          <Route 
            path="/login" 
            element={isAuthenticated ? <Navigate to="/" /> : <LoginPage />} 
          />
          <Route 
            path="/register" 
            element={isAuthenticated ? <Navigate to="/" /> : <RegisterPage />} 
          />
          
          {/* Chronione strony */}
          <Route
            path="/recipes"
            element={
              <ProtectedRoute>
                <RecipesPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/recipes/:id"
            element={
              <ProtectedRoute>
                <RecipeDetailPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <ProfilePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/active-pizza"
            element={
              <ProtectedRoute>
                <ActivePizzaPage />
              </ProtectedRoute>
            }
          />
          
          {/* 404 */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Suspense>
    </Layout>
  );
}

export default App;
