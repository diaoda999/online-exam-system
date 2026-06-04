import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {
  Container, Paper, Box, TextField, Button, Typography, Alert, MenuItem,
} from '@mui/material';
import { register as registerApi } from '../api/auth';
import type { RoleCode } from '../types';

const Register: React.FC = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: '',
    password: '',
    confirmPassword: '',
    realName: '',
    roleCode: 'STUDENT' as RoleCode,
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    if (form.password !== form.confirmPassword) {
      setError('两次密码不一致');
      return;
    }
    try {
      await registerApi({
        username: form.username,
        password: form.password,
        realName: form.realName,
        roleCode: form.roleCode,
      });
      setSuccess('注册成功，即将跳转到登录页面');
      setTimeout(() => navigate('/login'), 1500);
    } catch (err: any) {
      setError(err.message || '注册失败');
    }
  };

  return (
    <Container maxWidth="sm" sx={{ mt: 8 }}>
      <Paper sx={{ p: 4 }}>
        <Typography variant="h4" align="center" gutterBottom>
          用户注册
        </Typography>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

        <Box component="form" onSubmit={handleSubmit}>
          <TextField
            fullWidth margin="normal" label="用户名" required
            value={form.username}
            onChange={(e) => setForm({ ...form, username: e.target.value })}
          />
          <TextField
            fullWidth margin="normal" label="姓名" required
            value={form.realName}
            onChange={(e) => setForm({ ...form, realName: e.target.value })}
          />
          <TextField
            fullWidth margin="normal" label="密码" type="password" required
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
          />
          <TextField
            fullWidth margin="normal" label="确认密码" type="password" required
            value={form.confirmPassword}
            onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })}
          />
          <TextField
            fullWidth margin="normal" label="角色" select required
            value={form.roleCode}
            onChange={(e) => setForm({ ...form, roleCode: e.target.value as RoleCode })}
          >
            <MenuItem value="STUDENT">学生</MenuItem>
            <MenuItem value="TEACHER">教师</MenuItem>
          </TextField>
          <Button fullWidth variant="contained" type="submit" sx={{ mt: 2 }}>
            注册
          </Button>
          <Box sx={{ textAlign: 'center', mt: 2 }}>
            <Link to="/login" style={{ textDecoration: 'none' }}>
              <Typography variant="body2" color="primary">
                已有账号？点击登录
              </Typography>
            </Link>
          </Box>
        </Box>
      </Paper>
    </Container>
  );
};

export default Register;
