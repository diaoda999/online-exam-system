import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Register from './pages/Register';
import Users from './pages/Users';
import Courses from './pages/Courses';
import Classes from './pages/Classes';
import Questions from './pages/Questions';
import Banks from './pages/Banks';
import Papers from './pages/Papers';
import PaperCreate from './pages/PaperCreate';
import Exams from './pages/Exams';
import ExamCreate from './pages/ExamCreate';
import ExamRoom from './pages/ExamRoom';
import Grading from './pages/Grading';
import GradingDetail from './pages/GradingDetail';
import MyExams from './pages/MyExams';
import MyScores from './pages/MyScores';
import MyInvitations from './pages/MyInvitations';

const App: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="/courses" replace />} />
            <Route path="users" element={<ProtectedRoute roles={['ADMIN']}><Users /></ProtectedRoute>} />
            <Route path="courses" element={<ProtectedRoute roles={['ADMIN', 'TEACHER']}><Courses /></ProtectedRoute>} />
            <Route path="classes" element={<ProtectedRoute roles={['ADMIN', 'TEACHER']}><Classes /></ProtectedRoute>} />
            <Route path="questions" element={<ProtectedRoute roles={['ADMIN', 'TEACHER']}><Questions /></ProtectedRoute>} />
            <Route path="banks" element={<ProtectedRoute roles={['ADMIN', 'TEACHER']}><Banks /></ProtectedRoute>} />
            <Route path="papers" element={<ProtectedRoute roles={['ADMIN', 'TEACHER']}><Papers /></ProtectedRoute>} />
            <Route path="papers/create" element={<ProtectedRoute roles={['ADMIN', 'TEACHER']}><PaperCreate /></ProtectedRoute>} />
            <Route path="exams" element={<ProtectedRoute roles={['ADMIN', 'TEACHER']}><Exams /></ProtectedRoute>} />
            <Route path="exams/create" element={<ProtectedRoute roles={['ADMIN', 'TEACHER']}><ExamCreate /></ProtectedRoute>} />
            <Route path="grading" element={<ProtectedRoute roles={['ADMIN', 'TEACHER']}><Grading /></ProtectedRoute>} />
            <Route path="grading/:id" element={<ProtectedRoute roles={['ADMIN', 'TEACHER']}><GradingDetail /></ProtectedRoute>} />
            <Route path="my-exams" element={<ProtectedRoute roles={['STUDENT']}><MyExams /></ProtectedRoute>} />
            <Route path="my-scores" element={<ProtectedRoute roles={['STUDENT']}><MyScores /></ProtectedRoute>} />
            <Route path="my-invitations" element={<ProtectedRoute roles={['STUDENT']}><MyInvitations /></ProtectedRoute>} />
          </Route>
          {/* 考试答题页面独立布局（全屏） */}
          <Route path="/exam/:id" element={<ProtectedRoute roles={['STUDENT']}><ExamRoom /></ProtectedRoute>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App;
