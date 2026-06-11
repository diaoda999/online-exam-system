import React, { useEffect, useState, useCallback } from 'react';
import {
  Box, Button, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, MenuItem, IconButton, Snackbar, Alert, Tooltip, Typography,
} from '@mui/material';
import { DataGrid, type GridColDef, type GridPaginationModel } from '@mui/x-data-grid';
import { Edit as EditIcon, Delete as DeleteIcon, Visibility as VisibilityIcon, VisibilityOff as VisibilityOffIcon } from '@mui/icons-material';
import { listUsers, listUsersForAdmin, updateUser, deleteUser } from '../api/user';
import type { UserVO, AdminUserVO, RoleCode } from '../types';
import { useAuth } from '../contexts/AuthContext';

const roleMap: Record<string, string> = { ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生' };
const statusMap: Record<number, string> = { 1: '正常', 0: '禁用' };

const Users: React.FC = () => {
  const { user } = useAuth();
  const isAdmin = user?.roleCode === 'ADMIN';

  const [rows, setRows] = useState<(UserVO | AdminUserVO)[]>([]);
  const [total, setTotal] = useState(0);
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
  const [roleFilter, setRoleFilter] = useState('');
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' | 'warning' | 'info' });

  // 密码可见性
  const [visiblePasswords, setVisiblePasswords] = useState<Record<number, boolean>>({});

  // 编辑对话框
  const [editOpen, setEditOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<UserVO | AdminUserVO | null>(null);
  const [editForm, setEditForm] = useState({ realName: '', status: 1 });

  const fetchData = useCallback(async () => {
    try {
      const params = {
        roleCode: roleFilter || undefined,
        page: paginationModel.page + 1,
        size: paginationModel.pageSize,
      };
      if (isAdmin) {
        const data = await listUsersForAdmin(params);
        setRows(data.records);
        setTotal(data.total);
      } else {
        const data = await listUsers(params);
        setRows(data.records);
        setTotal(data.total);
      }
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  }, [paginationModel, roleFilter, isAdmin]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const togglePassword = (id: number) => {
    setVisiblePasswords((prev) => ({ ...prev, [id]: !prev[id] }));
  };

  const handleEdit = (u: UserVO | AdminUserVO) => {
    setEditingUser(u);
    setEditForm({ realName: u.realName, status: u.status });
    setEditOpen(true);
  };

  const handleSave = async () => {
    if (!editingUser) return;
    try {
      await updateUser(editingUser.id, editForm);
      setSnackbar({ open: true, message: '更新成功', severity: 'success' });
      setEditOpen(false);
      fetchData();
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('确定删除该用户？')) return;
    try {
      await deleteUser(id);
      setSnackbar({ open: true, message: '删除成功', severity: 'success' });
      fetchData();
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const columns: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 80 },
    { field: 'username', headerName: '用户名', width: 120 },
    ...(isAdmin ? [{
      field: 'plainPassword',
      headerName: '登录密码',
      width: 180,
      renderCell: (params: any) => {
        const row = params.row as AdminUserVO;
        const visible = visiblePasswords[row.id];
        return (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, width: '100%' }}>
            <Typography variant="body2" sx={{ flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', fontFamily: 'monospace', fontSize: 13 }}>
              {visible ? (row.plainPassword || '未记录') : '••••••••'}
            </Typography>
            <IconButton size="small" onClick={() => togglePassword(row.id)}>
              {visible ? <VisibilityOffIcon fontSize="small" /> : <VisibilityIcon fontSize="small" />}
            </IconButton>
          </Box>
        );
      },
    }] : []),
    { field: 'realName', headerName: '姓名', width: 120 },
    { field: 'roleName', headerName: '角色', width: 100 },
    { field: 'status', headerName: '状态', width: 80, renderCell: (params) => statusMap[params.value] || params.value },
    { field: 'createTime', headerName: '创建时间', width: 180 },
    {
      field: 'actions', headerName: '操作', width: 120, renderCell: (params) => (
        <Box>
          <IconButton size="small" onClick={() => handleEdit(params.row)}><EditIcon /></IconButton>
          <IconButton size="small" color="error" onClick={() => handleDelete(params.row.id)}><DeleteIcon /></IconButton>
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <TextField select size="small" label="角色筛选" value={roleFilter} onChange={(e) => { setRoleFilter(e.target.value); setPaginationModel({ ...paginationModel, page: 0 }); }} sx={{ width: 200 }}>
          <MenuItem value="">全部</MenuItem>
          <MenuItem value="ADMIN">管理员</MenuItem>
          <MenuItem value="TEACHER">教师</MenuItem>
          <MenuItem value="STUDENT">学生</MenuItem>
        </TextField>
      </Box>

      <Box sx={{ height: 600 }}>
        <DataGrid
          rows={rows} columns={columns} rowCount={total}
          paginationModel={paginationModel}
          onPaginationModelChange={setPaginationModel}
          paginationMode="server"
          pageSizeOptions={[5, 10, 20]}
        />
      </Box>

      <Dialog open={editOpen} onClose={() => setEditOpen(false)}>
        <DialogTitle>编辑用户</DialogTitle>
        <DialogContent sx={{ pt: 2, minWidth: 400 }}>
          <TextField fullWidth margin="dense" label="姓名" value={editForm.realName} onChange={(e) => setEditForm({ ...editForm, realName: e.target.value })} />
          <TextField fullWidth margin="dense" label="状态" select value={editForm.status} onChange={(e) => setEditForm({ ...editForm, status: Number(e.target.value) })}>
            <MenuItem value={1}>正常</MenuItem>
            <MenuItem value={0}>禁用</MenuItem>
          </TextField>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditOpen(false)}>取消</Button>
          <Button variant="contained" onClick={handleSave}>保存</Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={snackbar.open} autoHideDuration={3000} onClose={() => setSnackbar({ ...snackbar, open: false })}>
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
};

export default Users;
