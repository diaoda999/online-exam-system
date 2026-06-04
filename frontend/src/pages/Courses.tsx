import React, { useEffect, useState, useCallback } from 'react';
import {
  Box, Button, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, IconButton, Snackbar, Alert,
} from '@mui/material';
import { DataGrid, type GridColDef, type GridPaginationModel } from '@mui/x-data-grid';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { listCourses, createCourse, updateCourse, deleteCourse } from '../api/course';
import type { CourseVO, CourseCreateRequest, CourseUpdateRequest } from '../types';
import { useAuth } from '../contexts/AuthContext';

const Courses: React.FC = () => {
  const { user } = useAuth();
  const [rows, setRows] = useState<CourseVO[]>([]);
  const [total, setTotal] = useState(0);
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' | 'warning' | 'info' });

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState<CourseVO | null>(null);
  const [form, setForm] = useState({ courseName: '', courseCode: '', teacherId: 0, description: '' });

  const fetchData = useCallback(async () => {
    try {
      const data = await listCourses({
        teacherId: user?.roleCode === 'TEACHER' ? user.userId : undefined,
        page: paginationModel.page + 1,
        size: paginationModel.pageSize,
      });
      setRows(data.records);
      setTotal(data.total);
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  }, [paginationModel, user]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const openCreate = () => {
    setEditing(null);
    setForm({ courseName: '', courseCode: '', teacherId: user?.userId || 0, description: '' });
    setDialogOpen(true);
  };

  const openEdit = (course: CourseVO) => {
    setEditing(course);
    setForm({ courseName: course.courseName, courseCode: course.courseCode, teacherId: course.teacherId, description: course.description });
    setDialogOpen(true);
  };

  const handleSave = async () => {
    try {
      if (editing) {
        const req: CourseUpdateRequest = { courseName: form.courseName, courseCode: form.courseCode, description: form.description };
        await updateCourse(editing.id, req);
        setSnackbar({ open: true, message: '更新成功', severity: 'success' });
      } else {
        const req: CourseCreateRequest = { courseName: form.courseName, courseCode: form.courseCode, teacherId: form.teacherId, description: form.description };
        await createCourse(req);
        setSnackbar({ open: true, message: '创建成功', severity: 'success' });
      }
      setDialogOpen(false);
      fetchData();
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('确定删除该课程？')) return;
    try {
      await deleteCourse(id);
      setSnackbar({ open: true, message: '删除成功', severity: 'success' });
      fetchData();
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const columns: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 80 },
    { field: 'courseName', headerName: '课程名称', width: 180 },
    { field: 'courseCode', headerName: '课程编码', width: 120 },
    { field: 'teacherName', headerName: '授课教师', width: 120 },
    { field: 'description', headerName: '描述', width: 200, renderCell: (params) => params.value || '-' },
    { field: 'createTime', headerName: '创建时间', width: 180 },
    {
      field: 'actions', headerName: '操作', width: 120, renderCell: (params) => (
        <Box>
          <IconButton size="small" onClick={() => openEdit(params.row)}><EditIcon /></IconButton>
          <IconButton size="small" color="error" onClick={() => handleDelete(params.row.id)}><DeleteIcon /></IconButton>
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>创建课程</Button>
      </Box>

      <Box sx={{ height: 600 }}>
        <DataGrid rows={rows} columns={columns} rowCount={total} paginationModel={paginationModel} onPaginationModelChange={setPaginationModel} paginationMode="server" pageSizeOptions={[5, 10, 20]} />
      </Box>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editing ? '编辑课程' : '创建课程'}</DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          <TextField fullWidth margin="dense" label="课程名称" value={form.courseName} onChange={(e) => setForm({ ...form, courseName: e.target.value })} />
          <TextField fullWidth margin="dense" label="课程编码" value={form.courseCode} onChange={(e) => setForm({ ...form, courseCode: e.target.value })} />
          <TextField fullWidth margin="dense" label="描述" multiline rows={2} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>取消</Button>
          <Button variant="contained" onClick={handleSave}>保存</Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={snackbar.open} autoHideDuration={3000} onClose={() => setSnackbar({ ...snackbar, open: false })}>
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
};

export default Courses;
