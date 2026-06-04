import React, { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  AppBar, Toolbar, Typography, Drawer, List, ListItemButton,
  ListItemIcon, ListItemText, Box, Avatar, Menu, MenuItem, IconButton,
  Divider,
} from '@mui/material';
import {
  School, People, Class as ClassIcon, MenuBook, Inventory2,
  Description, Assignment, Grading, Quiz, Score, Logout,
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import type { RoleCode } from '../types';

const DRAWER_WIDTH = 240;

interface MenuItemConfig {
  text: string;
  icon: React.ReactElement;
  path: string;
  roles: RoleCode[];
}

const menuItems: MenuItemConfig[] = [
  { text: '课程管理', icon: <School />, path: '/courses', roles: ['ADMIN', 'TEACHER'] },
  { text: '班级管理', icon: <ClassIcon />, path: '/classes', roles: ['ADMIN', 'TEACHER'] },
  { text: '题目管理', icon: <MenuBook />, path: '/questions', roles: ['ADMIN', 'TEACHER'] },
  { text: '题库管理', icon: <Inventory2 />, path: '/banks', roles: ['ADMIN', 'TEACHER'] },
  { text: '试卷管理', icon: <Description />, path: '/papers', roles: ['ADMIN', 'TEACHER'] },
  { text: '考试管理', icon: <Assignment />, path: '/exams', roles: ['ADMIN', 'TEACHER'] },
  { text: '批改管理', icon: <Grading />, path: '/grading', roles: ['ADMIN', 'TEACHER'] },
  { text: '用户管理', icon: <People />, path: '/users', roles: ['ADMIN'] },
  { text: '我的考试', icon: <Quiz />, path: '/my-exams', roles: ['STUDENT'] },
  { text: '我的成绩', icon: <Score />, path: '/my-scores', roles: ['STUDENT'] },
];

const Layout: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  const roleLabel = user?.roleCode === 'ADMIN' ? '管理员' : user?.roleCode === 'TEACHER' ? '教师' : '学生';

  const filteredMenuItems = menuItems.filter(
    (item) => user?.roleCode && item.roles.includes(user.roleCode)
  );

  const handleLogout = () => {
    setAnchorEl(null);
    logout();
    navigate('/login');
  };

  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
        <Toolbar>
          <Typography variant="h6" noWrap sx={{ flexGrow: 1 }}>
            在线考试系统
          </Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="body2" color="inherit">
              {user?.realName} ({roleLabel})
            </Typography>
            <IconButton color="inherit" onClick={(e) => setAnchorEl(e.currentTarget)}>
              <Avatar sx={{ width: 32, height: 32, bgcolor: 'secondary.main' }}>
                {user?.realName?.[0] || 'U'}
              </Avatar>
            </IconButton>
            <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={() => setAnchorEl(null)}>
              <MenuItem disabled>
                <Typography variant="body2">{user?.username}</Typography>
              </MenuItem>
              <Divider />
              <MenuItem onClick={handleLogout}>
                <ListItemIcon><Logout fontSize="small" /></ListItemIcon>
                退出登录
              </MenuItem>
            </Menu>
          </Box>
        </Toolbar>
      </AppBar>

      <Drawer
        variant="permanent"
        sx={{
          width: DRAWER_WIDTH,
          flexShrink: 0,
          '& .MuiDrawer-paper': { width: DRAWER_WIDTH, boxSizing: 'border-box' },
        }}
      >
        <Toolbar />
        <List>
          {filteredMenuItems.map((item) => (
            <ListItemButton
              key={item.path}
              selected={location.pathname === item.path}
              onClick={() => navigate(item.path)}
            >
              <ListItemIcon>{item.icon}</ListItemIcon>
              <ListItemText primary={item.text} />
            </ListItemButton>
          ))}
        </List>
      </Drawer>

      <Box component="main" sx={{ flexGrow: 1, p: 3, bgcolor: '#f5f5f5', minHeight: '100vh' }}>
        <Toolbar />
        <Outlet />
      </Box>
    </Box>
  );
};

export default Layout;
