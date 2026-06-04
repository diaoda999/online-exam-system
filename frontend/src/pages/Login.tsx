import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {
  Container, Paper, Box, TextField, Button, Typography, Alert, MenuItem,
} from '@mui/material';
import { login as loginApi } from '../api/auth';
import { useAuth } from '../contexts/AuthContext';
import type { RoleCode } from '../types';

const Login: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      const data = await loginApi(form);
      login(data.token, {
        userId: data.userId,
        username: data.username,
        realName: data.realName,
        roleCode: data.roleCode as RoleCode,
      });
      navigate('/');
    } catch (err: any) {
      setError(err.message || '登录失败');
    }
  };

  return (
    <Container maxWidth="sm" sx={{ mt: 8 }}>
      <Paper sx={{ p: 4 }}>
        <Typography variant="h4" align="center" gutterBottom>
          在线考试系统
        </Typography>
        <Typography variant="h6" align="center" color="text.secondary" gutterBottom>
          用户登录
        </Typography>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

        <Box component="form" onSubmit={handleSubmit}>
          <TextField
            fullWidth margin="normal" label="用户名" required
            value={form.username}
            onChange={(e) => setForm({ ...form, username: e.target.value })}
          />
          <TextField
            fullWidth margin="normal" label="密码" type="password" required
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
          />
          <Button fullWidth variant="contained" type="submit" sx={{ mt: 2 }}>
            登录
          </Button>
          <Box sx={{ textAlign: 'center', mt: 2 }}>
            <Link to="/register" style={{ textDecoration: 'none' }}>
              <Typography variant="body2" color="primary">
                没有账号？点击注册
              </Typography>
            </Link>
          </Box>
        </Box>
      </Paper>
    </Container>
  );
};

export default Login;
